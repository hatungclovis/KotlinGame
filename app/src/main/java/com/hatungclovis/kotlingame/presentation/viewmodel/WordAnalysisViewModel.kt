package com.hatungclovis.kotlingame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hatungclovis.kotlingame.domain.models.WordAnalysis
import com.hatungclovis.kotlingame.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordAnalysisViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _wordAnalysis = MutableStateFlow(WordAnalysis())
    val wordAnalysis: StateFlow<WordAnalysis> = _wordAnalysis.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedLength = MutableStateFlow<Int?>(null)
    val selectedLength: StateFlow<Int?> = _selectedLength.asStateFlow()

    private val _selectedLetter = MutableStateFlow<String?>(null)
    val selectedLetter: StateFlow<String?> = _selectedLetter.asStateFlow()

    private val _wordsForSelection = MutableStateFlow<List<String>>(emptyList())
    val wordsForSelection: StateFlow<List<String>> = _wordsForSelection.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Cache for all words to avoid repeated loading
    private var allWords: List<String> = emptyList()
    private var commonWords: List<String> = emptyList()

    /**
     * Load word analysis data
     */
    fun loadWordAnalysis() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Load words if not already loaded
                if (allWords.isEmpty()) {
                    allWords = wordRepository.getAllWords()
                    commonWords = wordRepository.getCommonWords()
                }

                // Generate analysis
                val analysis = generateWordAnalysis(commonWords)
                _wordAnalysis.value = analysis

            } catch (e: Exception) {
                _message.value = "Failed to load word analysis: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Select a word length to view words
     */
    fun selectLength(length: Int) {
        viewModelScope.launch {
            try {
                _selectedLength.value = length
                _selectedLetter.value = null

                val wordsWithLength = commonWords.filter { it.length == length }
                _wordsForSelection.value = wordsWithLength.sorted()

            } catch (e: Exception) {
                _message.value = "Failed to load words for length $length: ${e.message}"
            }
        }
    }

    /**
     * Select a first letter to view words
     */
    fun selectLetter(letter: String) {
        viewModelScope.launch {
            try {
                _selectedLetter.value = letter
                _selectedLength.value = null

                val wordsWithLetter = commonWords.filter { 
                    it.first().toString().equals(letter, ignoreCase = true)
                }
                _wordsForSelection.value = wordsWithLetter.sorted()

            } catch (e: Exception) {
                _message.value = "Failed to load words starting with '$letter': ${e.message}"
            }
        }
    }

    /**
     * Clear selection
     */
    fun clearSelection() {
        _selectedLength.value = null
        _selectedLetter.value = null
        _wordsForSelection.value = emptyList()
    }

    /**
     * Generate word analysis from word lists
     */
    private fun generateWordAnalysis(commonWords: List<String>): WordAnalysis {
        val lengthDistribution = mutableMapOf<Int, Int>()
        val letterDistribution = mutableMapOf<String, Int>()

        commonWords.forEach { word ->
            // Count by length
            val length = word.length
            lengthDistribution[length] = lengthDistribution.getOrDefault(length, 0) + 1

            // Count by first letter
            val firstLetter = word.first().toString().lowercase()
            letterDistribution[firstLetter] = letterDistribution.getOrDefault(firstLetter, 0) + 1
        }

        return WordAnalysis(
            byLength = lengthDistribution,
            byFirstLetter = letterDistribution
        )
    }

    /**
     * Clear message
     */
    fun clearMessage() {
        _message.value = null
    }
}
