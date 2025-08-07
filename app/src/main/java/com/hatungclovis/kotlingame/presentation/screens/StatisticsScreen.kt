package com.hatungclovis.kotlingame.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hatungclovis.kotlingame.domain.models.DifficultyLevel
import com.hatungclovis.kotlingame.presentation.viewmodel.StatisticsViewModel
import com.hatungclovis.kotlingame.utils.GameUtils
import com.hatungclovis.kotlingame.utils.HapticUtils
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val statistics by viewModel.statistics.collectAsState()
    val gameHistory by viewModel.gameHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
        viewModel.loadGameHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistics",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            HapticUtils.light(context)
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            HapticUtils.light(context)
                            GameUtils.shareStatistics(context, statistics)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Statistics"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Overall Statistics Cards
                item {
                    OverallStatisticsSection(statistics = statistics)
                }

                // Win Percentage Chart
                if (statistics.gamesPlayed > 0) {
                    item {
                        WinPercentageCard(
                            winPercentage = statistics.winPercentage,
                            gamesWon = statistics.gamesWon,
                            gamesPlayed = statistics.gamesPlayed
                        )
                    }
                }

                // Guess Distribution Chart
                if (statistics.guessDistribution.isNotEmpty()) {
                    item {
                        GuessDistributionCard(
                            distribution = statistics.guessDistribution,
                            totalGames = statistics.gamesWon
                        )
                    }
                }

                // Performance by Difficulty
                if (gameHistory.isNotEmpty()) {
                    item {
                        DifficultyPerformanceCard(gameHistory = gameHistory)
                    }
                }

                // Recent Games
                if (gameHistory.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Games",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(gameHistory.take(10)) { game ->
                        RecentGameCard(game = game)
                    }
                }

                // Reset Statistics Button
                item {
                    var showResetDialog by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = {
                            HapticUtils.impact(context)
                            showResetDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Statistics")
                    }

                    if (showResetDialog) {
                        AlertDialog(
                            onDismissRequest = { showResetDialog = false },
                            title = { Text("Reset Statistics") },
                            text = {
                                Text("Are you sure you want to reset all statistics and game history? This action cannot be undone.")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        HapticUtils.success(context)
                                        viewModel.resetAllStatistics()
                                        showResetDialog = false
                                    }
                                ) {
                                    Text("Reset", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showResetDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OverallStatisticsSection(
    statistics: com.hatungclovis.kotlingame.domain.models.GameStatistics
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Overall Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = statistics.gamesPlayed.toString(),
                    label = "Played",
                    icon = Icons.Default.Games
                )
                StatisticItem(
                    value = "${statistics.winPercentage.toInt()}%",
                    label = "Win Rate",
                    icon = Icons.Default.TrendingUp
                )
                StatisticItem(
                    value = statistics.currentStreak.toString(),
                    label = "Current",
                    icon = Icons.Default.LocalFire
                )
                StatisticItem(
                    value = statistics.maxStreak.toString(),
                    label = "Best",
                    icon = Icons.Default.Star
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = statistics.averageScore.toInt().toString(),
                    label = "Avg Score",
                    icon = Icons.Default.Score
                )
                StatisticItem(
                    value = String.format("%.1f", statistics.averageGuesses),
                    label = "Avg Guesses",
                    icon = Icons.Default.Numbers
                )
                StatisticItem(
                    value = statistics.totalScore.toString(),
                    label = "Total Score",
                    icon = Icons.Default.EmojiEvents
                )
                StatisticItem(
                    value = GameUtils.formatTime(statistics.totalTime ?: 0L),
                    label = "Total Time",
                    icon = Icons.Default.Timer
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WinPercentageCard(
    winPercentage: Double,
    gamesWon: Int,
    gamesPlayed: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Win Percentage",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressChart(
                    percentage = winPercentage.toFloat(),
                    size = 120.dp,
                    strokeWidth = 12.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$gamesWon wins out of $gamesPlayed games",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CircularProgressChart(
    percentage: Float,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000),
        label = "percentage"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasSize = size.toPx()
            val stroke = strokeWidth.toPx()
            val radius = (canvasSize - stroke) / 2

            // Background circle
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
            )

            // Progress arc
            drawArc(
                color = Color(0xFF6AAA64),
                startAngle = -90f,
                sweepAngle = 360f * (animatedPercentage / 100f),
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = stroke,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }

        Text(
            text = "${animatedPercentage.toInt()}%",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GuessDistributionCard(
    distribution: Map<Int, Int>,
    totalGames: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Guess Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val maxCount = distribution.values.maxOrNull() ?: 1

            distribution.toSortedMap().forEach { (guesses, count) ->
                GuessDistributionBar(
                    guesses = guesses,
                    count = count,
                    maxCount = maxCount,
                    percentage = if (totalGames > 0) (count * 100f / totalGames) else 0f
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun GuessDistributionBar(
    guesses: Int,
    count: Int,
    maxCount: Int,
    percentage: Float
) {
    val animatedWidth by animateFloatAsState(
        targetValue = if (maxCount > 0) count.toFloat() / maxCount else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "bar_width"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = guesses.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(20.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        ) {
            // Background bar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Count text overlay
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 8.dp)
            )
        }

        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(30.dp)
        )
    }
}

@Composable
fun DifficultyPerformanceCard(
    gameHistory: List<com.hatungclovis.kotlingame.domain.models.GameHistory>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Performance by Difficulty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val performanceByDifficulty = gameHistory.groupBy { it.difficulty }
            
            DifficultyLevel.values().forEach { difficulty ->
                val games = performanceByDifficulty[difficulty] ?: emptyList()
                val wins = games.count { it.won }
                val winRate = if (games.isNotEmpty()) (wins * 100f / games.size) else 0f

                DifficultyPerformanceRow(
                    difficulty = difficulty,
                    gamesPlayed = games.size,
                    winRate = winRate,
                    averageScore = games.map { it.score }.average().takeIf { !it.isNaN() } ?: 0.0
                )
                
                if (difficulty != DifficultyLevel.values().last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DifficultyPerformanceRow(
    difficulty: DifficultyLevel,
    gamesPlayed: Int,
    winRate: Float,
    averageScore: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${difficulty.label} (${difficulty.attempts} attempts)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$gamesPlayed games played",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${winRate.toInt()}% win rate",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Avg: ${averageScore.toInt()} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun RecentGameCard(
    game: com.hatungclovis.kotlingame.domain.models.GameHistory
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (game.won) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (game.won) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (game.won) Color(0xFF6AAA64) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = "${game.difficulty.label} • ${game.wordLength} letters",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (game.won) {
                            "Won in ${game.attempts} ${if (game.attempts == 1) "attempt" else "attempts"}"
                        } else {
                            "Lost • Word was '${game.targetWord}'"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${game.score} pts",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = GameUtils.formatTime(game.playTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
