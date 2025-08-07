package com.hatungclovis.kotlingame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hatungclovis.kotlingame.domain.models.DifficultyLevel
import com.hatungclovis.kotlingame.domain.models.GameSettings
import com.hatungclovis.kotlingame.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(
        GameSettings(
            difficulty = DifficultyLevel.MEDIUM,
            wordLength = 5,
            hapticEnabled = true,
            darkMode = false,
            soundEnabled = true
        )
    )
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Load settings from repository
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val savedSettings = gameRepository.getSettings()
                if (savedSettings != null) {
                    _settings.value = savedSettings
                }
            } catch (e: Exception) {
                // Use default settings if loading fails
                _message.value = "Failed to load settings: ${e.message}"
            }
        }
    }

    /**
     * Update dark mode setting
     */
    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val updatedSettings = _settings.value.copy(darkMode = enabled)
                _settings.value = updatedSettings
                gameRepository.saveSettings(updatedSettings)
                _message.value = if (enabled) "Dark mode enabled" else "Light mode enabled"
                clearMessage()
            } catch (e: Exception) {
                _message.value = "Failed to update theme: ${e.message}"
            }
        }
    }

    /**
     * Update haptic feedback setting
     */
    fun updateHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val updatedSettings = _settings.value.copy(hapticEnabled = enabled)
                _settings.value = updatedSettings
                gameRepository.saveSettings(updatedSettings)
                _message.value = if (enabled) "Haptic feedback enabled" else "Haptic feedback disabled"
                clearMessage()
            } catch (e: Exception) {
                _message.value = "Failed to update haptic setting: ${e.message}"
            }
        }
    }

    /**
     * Update sound effects setting
     */
    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val updatedSettings = _settings.value.copy(soundEnabled = enabled)
                _settings.value = updatedSettings
                gameRepository.saveSettings(updatedSettings)
                _message.value = if (enabled) "Sound effects enabled" else "Sound effects disabled"
                clearMessage()
            } catch (e: Exception) {
                _message.value = "Failed to update sound setting: ${e.message}"
            }
        }
    }

    /**
     * Update default difficulty
     */
    fun updateDefaultDifficulty(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            try {
                val updatedSettings = _settings.value.copy(difficulty = difficulty)
                _settings.value = updatedSettings
                gameRepository.saveSettings(updatedSettings)
                _message.value = "Default difficulty set to ${difficulty.label}"
                clearMessage()
            } catch (e: Exception) {
                _message.value = "Failed to update difficulty: ${e.message}"
            }
        }
    }

    /**
     * Update default word length
     */
    fun updateDefaultWordLength(length: Int) {
        viewModelScope.launch {
            try {
                val updatedSettings = _settings.value.copy(wordLength = length)
                _settings.value = updatedSettings
                gameRepository.saveSettings(updatedSettings)
                _message.value = "Default word length set to $length"
                clearMessage()
            } catch (e: Exception) {
                _message.value = "Failed to update word length: ${e.message}"
            }
        }
    }

    /**
     * Reset all statistics
     */
    fun resetStatistics() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                gameRepository.resetStatistics()
                _message.value = "Statistics reset successfully"
                clearMessage()
            } catch (e: Exception) {
                _message.value = "Failed to reset statistics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get current settings
     */
    fun getCurrentSettings(): GameSettings = _settings.value

    /**
     * Clear message after a delay
     */
    private fun clearMessage() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _message.value = null
        }
    }

    /**
     * Manually clear message
     */
    fun clearMessage() {
        _message.value = null
    }
}
