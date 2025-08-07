package com.hatungclovis.kotlingame.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hatungclovis.kotlingame.domain.models.*
import com.hatungclovis.kotlingame.domain.repository.GameRepository
import com.hatungclovis.kotlingame.domain.repository.WordRepository
import com.hatungclovis.kotlingame.presentation.components.*
import com.hatungclovis.kotlingame.presentation.theme.*
import com.hatungclovis.kotlingame.utils.triggerLightHaptic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home Screen ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val gameRepository: GameRepository
) : ViewModel() {
    
    private val _uiState = mutableStateOf(HomeUiState())
    val uiState: State<HomeUiState> = _uiState
    
    data class HomeUiState(
        val isLoading: Boolean = true,
        val error: String? = null,
        val gameSettings: GameSettings = GameSettings(),
        val gameStatistics: GameStatistics = GameStatistics(),
        val availableWordLengths: List<Int> = emptyList(),
        val selectedDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        val selectedWordLength: Int = 5,
        val showError: Boolean = false
    )
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load game settings
                val settingsResult = gameRepository.getGameSettings()
                val settings = settingsResult.getOrNull() ?: GameSettings()
                
                // Load game statistics
                val statsResult = gameRepository.getGameStatistics()
                val stats = statsResult.getOrNull() ?: GameStatistics()
                
                // Load available word lengths
                val wordLengthsResult = wordRepository.getAvailableWordLengths()
                val wordLengths = wordLengthsResult.getOrNull() ?: GameConstants.WORD_LENGTH_OPTIONS
                
                // Validate word files
                val validationResult = wordRepository.validateWordFiles()
                if (validationResult.isFailure) {
                    throw Exception("Word files validation failed: ${validationResult.exceptionOrNull()?.message}")
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    gameSettings = settings,
                    gameStatistics = stats,
                    availableWordLengths = wordLengths,
                    selectedDifficulty = settings.difficulty,
                    selectedWordLength = settings.wordLength,
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun selectDifficulty(difficulty: DifficultyLevel) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
    }
    
    fun selectWordLength(length: Int) {
        _uiState.value = _uiState.value.copy(selectedWordLength = length)
    }
    
    fun startNewGame(onNavigateToGame: (DifficultyLevel, Int) -> Unit) {
        viewModelScope.launch {
            try {
                // Save selected settings
                val newSettings = _uiState.value.gameSettings.copy(
                    difficulty = _uiState.value.selectedDifficulty,
                    wordLength = _uiState.value.selectedWordLength
                )
                gameRepository.updateGameSettings(newSettings)
                
                // Navigate to game
                onNavigateToGame(_uiState.value.selectedDifficulty, _uiState.value.selectedWordLength)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start game: ${e.message}",
                    showError = true
                )
            }
        }
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(showError = false)
    }
    
    fun retry() {
        loadInitialData()
    }
}

/**
 * Home Screen
 */
@Composable
fun HomeScreen(
    onNavigateToGame: (DifficultyLevel, Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToWordAnalysis: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            HomeHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            when {
                uiState.isLoading -> {
                    GameLoadingIndicator(
                        message = "Loading game...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                uiState.error != null -> {
                    ErrorSection(
                        error = uiState.error,
                        onRetry = viewModel::retry
                    )
                }
                
                else -> {
                    // Quick Stats
                    QuickStatsSection(
                        statistics = uiState.gameStatistics,
                        onViewStatistics = onNavigateToStatistics
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Difficulty Selection
                    DifficultySelection(
                        selectedDifficulty = uiState.selectedDifficulty,
                        onDifficultySelected = { difficulty ->
                            context.triggerLightHaptic()
                            viewModel.selectDifficulty(difficulty)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Word Length Selection
                    WordLengthSelection(
                        availableLengths = uiState.availableWordLengths,
                        selectedLength = uiState.selectedWordLength,
                        onLengthSelected = { length ->
                            context.triggerLightHaptic()
                            viewModel.selectWordLength(length)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Start Game Button
                    StartGameButton(
                        difficulty = uiState.selectedDifficulty,
                        wordLength = uiState.selectedWordLength,
                        onStartGame = {
                            context.triggerLightHaptic()
                            viewModel.startNewGame(onNavigateToGame)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Navigation Buttons
                    NavigationSection(
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToWordAnalysis = onNavigateToWordAnalysis
                    )
                }
            }
        }
        
        // Error Toast
        GameToast(
            message = uiState.error ?: "",
            isVisible = uiState.showError,
            onDismiss = viewModel::dismissError,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        )
    }
}

@Composable
private fun HomeHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Kotlin Wordle",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Enhanced Word Guessing Game",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun QuickStatsSection(
    statistics: GameStatistics,
    onViewStatistics: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticCard(
                title = "Played",
                value = statistics.gamesPlayed.toString(),
                modifier = Modifier.weight(1f)
            )
            
            StatisticCard(
                title = "Win %",
                value = "${statistics.winPercentage.toInt()}",
                subtitle = if (statistics.currentStreak > 0) "Streak: ${statistics.currentStreak}" else null,
                modifier = Modifier.weight(1f)
            )
            
            StatisticCard(
                title = "Avg Score",
                value = if (statistics.averageScore > 0) "${statistics.averageScore.toInt()}" else "-",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(onClick = onViewStatistics) {
            Text("View Detailed Statistics")
        }
    }
}

@Composable
private fun DifficultySelection(
    selectedDifficulty: DifficultyLevel,
    onDifficultySelected: (DifficultyLevel) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Difficulty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyLevel.values().forEach { difficulty ->
                DifficultyChip(
                    difficulty = difficulty,
                    isSelected = selectedDifficulty == difficulty,
                    onSelected = { onDifficultySelected(difficulty) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DifficultyChip(
    difficulty: DifficultyLevel,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = GameComponentShapes.SelectionChip,
        modifier = modifier
            .clickable { onSelected() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = difficulty.label,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${difficulty.attempts} attempts",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                }
            )
        }
    }
}

@Composable
private fun WordLengthSelection(
    availableLengths: List<Int>,
    selectedLength: Int,
    onLengthSelected: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Word Length",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(availableLengths) { length ->
                WordLengthChip(
                    length = length,
                    isSelected = selectedLength == length,
                    onSelected = { onLengthSelected(length) }
                )
            }
        }
    }
}

@Composable
private fun WordLengthChip(
    length: Int,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable { onSelected() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = length.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StartGameButton(
    difficulty: DifficultyLevel,
    wordLength: Int,
    onStartGame: () -> Unit
) {
    Button(
        onClick = onStartGame,
        shape = GameComponentShapes.Button,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Start New Game",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${difficulty.label} • $wordLength letters",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun NavigationSection(
    onNavigateToSettings: () -> Unit,
    onNavigateToWordAnalysis: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationButton(
            icon = Icons.Default.Settings,
            label = "Settings",
            onClick = onNavigateToSettings,
            modifier = Modifier.weight(1f)
        )
        
        NavigationButton(
            icon = Icons.Default.Analytics,
            label = "Word Analysis",
            onClick = onNavigateToWordAnalysis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NavigationButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = GameComponentShapes.Button,
        modifier = modifier.height(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ErrorSection(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error Loading Game",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    KotlinGameTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeHeader()
                
                QuickStatsSection(
                    statistics = GameStatistics(
                        gamesPlayed = 42,
                        gamesWon = 35,
                        winPercentage = 83.3,
                        currentStreak = 5,
                        averageScore = 67.5
                    ),
                    onViewStatistics = { }
                )
                
                DifficultySelection(
                    selectedDifficulty = DifficultyLevel.MEDIUM,
                    onDifficultySelected = { }
                )
                
                WordLengthSelection(
                    availableLengths = listOf(3, 4, 5, 6, 7, 8),
                    selectedLength = 5,
                    onLengthSelected = { }
                )
                
                StartGameButton(
                    difficulty = DifficultyLevel.MEDIUM,
                    wordLength = 5,
                    onStartGame = { }
                )
            }
        }
    }
}
