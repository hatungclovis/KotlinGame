package com.hatungclovis.kotlingame.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hatungclovis.kotlingame.domain.models.WordAnalysis
import com.hatungclovis.kotlingame.presentation.viewmodel.WordAnalysisViewModel
import com.hatungclovis.kotlingame.utils.HapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordAnalysisScreen(
    navController: NavController,
    viewModel: WordAnalysisViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val wordAnalysis by viewModel.wordAnalysis.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedLength by viewModel.selectedLength.collectAsState()
    val selectedLetter by viewModel.selectedLetter.collectAsState()
    val wordsForSelection by viewModel.wordsForSelection.collectAsState()
    
    var showWordListDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadWordAnalysis()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Word Analysis",
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
                    // Start Playing button
                    TextButton(
                        onClick = {
                            HapticUtils.impact(context)
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play")
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Overview Card
                item {
                    OverviewCard(wordAnalysis = wordAnalysis)
                }

                // Word Length Distribution
                item {
                    WordLengthDistributionCard(
                        distribution = wordAnalysis.byLength,
                        onLengthClick = { length ->
                            HapticUtils.light(context)
                            viewModel.selectLength(length)
                            showWordListDialog = true
                        }
                    )
                }

                // First Letter Distribution  
                item {
                    FirstLetterDistributionCard(
                        distribution = wordAnalysis.byFirstLetter,
                        onLetterClick = { letter ->
                            HapticUtils.light(context)
                            viewModel.selectLetter(letter)
                            showWordListDialog = true
                        }
                    )
                }

                // Insights Card
                item {
                    InsightsCard(wordAnalysis = wordAnalysis)
                }

                // Tips Card
                item {
                    TipsCard()
                }
            }
        }

        // Word List Dialog
        if (showWordListDialog) {
            WordListDialog(
                title = when {
                    selectedLength != null -> "Words with ${selectedLength} letters"
                    selectedLetter != null -> "Words starting with '${selectedLetter?.uppercase()}'"
                    else -> "Words"
                },
                words = wordsForSelection,
                onDismiss = { 
                    showWordListDialog = false
                    viewModel.clearSelection()
                }
            )
        }
    }
}

@Composable
fun OverviewCard(wordAnalysis: WordAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Word Database Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewStatItem(
                    value = "2,992",
                    label = "Common Words",
                    subtitle = "High frequency"
                )
                OverviewStatItem(
                    value = "370,105",
                    label = "Total Words", 
                    subtitle = "Dictionary"
                )
                OverviewStatItem(
                    value = "3-14",
                    label = "Length Range",
                    subtitle = "Letters"
                )
            }
        }
    }
}

@Composable
fun OverviewStatItem(
    value: String,
    label: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WordLengthDistributionCard(
    distribution: Map<Int, Int>,
    onLengthClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Distribution by Word Length",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (distribution.isNotEmpty()) {
                val maxCount = distribution.values.maxOrNull() ?: 1

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(distribution.toSortedMap().toList()) { (length, count) ->
                        LengthBarChart(
                            length = length,
                            count = count,
                            maxCount = maxCount,
                            onClick = { onLengthClick(length) }
                        )
                    }
                }
            } else {
                Text(
                    text = "No word length data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun LengthBarChart(
    length: Int,
    count: Int,
    maxCount: Int,
    onClick: () -> Unit
) {
    val animatedHeight by animateFloatAsState(
        targetValue = if (maxCount > 0) count.toFloat() / maxCount else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "bar_height"
    )
    
    val maxBarHeight = 80.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // Count label
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Bar
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(maxBarHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedHeight)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Length label
        Text(
            text = length.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FirstLetterDistributionCard(
    distribution: Map<String, Int>,
    onLetterClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Distribution by First Letter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.FormatLetterCase,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (distribution.isNotEmpty()) {
                val sortedDistribution = distribution.toList().sortedBy { it.first }
                val maxCount = distribution.values.maxOrNull() ?: 1

                // Split into rows of 6-7 letters each
                val rows = sortedDistribution.chunked(7)
                
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { (letter, count) ->
                            LetterDistributionItem(
                                letter = letter,
                                count = count,
                                maxCount = maxCount,
                                onClick = { onLetterClick(letter) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                Text(
                    text = "No first letter data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun LetterDistributionItem(
    letter: String,
    count: Int,
    maxCount: Int,
    onClick: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (maxCount > 0) 0.3f + (count.toFloat() / maxCount * 0.7f) else 0.3f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InsightsCard(wordAnalysis: WordAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Strategic Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Most common word length
            val mostCommonLength = wordAnalysis.byLength.maxByOrNull { it.value }
            if (mostCommonLength != null) {
                InsightItem(
                    icon = Icons.Default.TrendingUp,
                    title = "Most Common Length",
                    description = "${mostCommonLength.key} letters (${mostCommonLength.value} words)"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Most common first letter
            val mostCommonLetter = wordAnalysis.byFirstLetter.maxByOrNull { it.value }
            if (mostCommonLetter != null) {
                InsightItem(
                    icon = Icons.Default.Grade,
                    title = "Most Common First Letter",
                    description = "'${mostCommonLetter.key.uppercase()}' (${mostCommonLetter.value} words)"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Least common first letter
            val leastCommonLetter = wordAnalysis.byFirstLetter.minByOrNull { it.value }
            if (leastCommonLetter != null) {
                InsightItem(
                    icon = Icons.Default.Flare,
                    title = "Least Common First Letter",
                    description = "'${leastCommonLetter.key.uppercase()}' (${leastCommonLetter.value} words)"
                )
            }
        }
    }
}

@Composable
fun InsightItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Strategy Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val tips = listOf(
                "Start with common vowels (A, E, I, O, U) to quickly identify letter positions",
                "Try words with frequent consonants (R, N, T, L, S) early in the game",
                "5-letter words are most common - great for beginners to intermediate players",
                "Use the letter frequency data above to make strategic first guesses",
                "Words starting with 'S' are very common - consider them for difficult puzzles"
            )
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListDialog(
    title: String,
    words: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(words.take(50)) { word -> // Show max 50 words
                    Text(
                        text = word.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                if (words.size > 50) {
                    item {
                        Text(
                            text = "... and ${words.size - 50} more words",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
