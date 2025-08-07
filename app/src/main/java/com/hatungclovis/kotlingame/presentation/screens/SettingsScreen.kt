package com.hatungclovis.kotlingame.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hatungclovis.kotlingame.domain.models.DifficultyLevel
import com.hatungclovis.kotlingame.domain.models.GameConstants
import com.hatungclovis.kotlingame.presentation.components.DifficultyChip
import com.hatungclovis.kotlingame.presentation.components.WordLengthChip
import com.hatungclovis.kotlingame.presentation.viewmodel.SettingsViewModel
import com.hatungclovis.kotlingame.utils.HapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance Section
            SettingsSection(
                title = "Appearance",
                icon = Icons.Default.Palette
            ) {
                // Dark Mode Toggle
                SettingItem(
                    title = "Dark Mode",
                    description = "Switch between light and dark themes",
                    icon = if (settings.darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    trailing = {
                        Switch(
                            checked = settings.darkMode,
                            onCheckedChange = { enabled ->
                                HapticUtils.light(context)
                                viewModel.updateDarkMode(enabled)
                            }
                        )
                    }
                )
            }

            // Gameplay Section
            SettingsSection(
                title = "Gameplay",
                icon = Icons.Default.Games
            ) {
                // Default Difficulty
                SettingItem(
                    title = "Default Difficulty",
                    description = "Choose your preferred difficulty level",
                    icon = Icons.Default.Speed
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        DifficultyLevel.values().forEach { difficulty ->
                            DifficultyChip(
                                difficulty = difficulty,
                                isSelected = settings.difficulty == difficulty,
                                onClick = {
                                    HapticUtils.light(context)
                                    viewModel.updateDefaultDifficulty(difficulty)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Default Word Length
                SettingItem(
                    title = "Default Word Length",
                    description = "Choose your preferred word length",
                    icon = Icons.Default.FormatSize
                ) {
                    LazyHorizontalScrollableRow(
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        GameConstants.WORD_LENGTH_OPTIONS.forEach { length ->
                            WordLengthChip(
                                length = length,
                                isSelected = settings.wordLength == length,
                                onClick = {
                                    HapticUtils.light(context)
                                    viewModel.updateDefaultWordLength(length)
                                }
                            )
                        }
                    }
                }
            }

            // Feedback Section
            SettingsSection(
                title = "Feedback",
                icon = Icons.Default.Vibration
            ) {
                // Haptic Feedback
                SettingItem(
                    title = "Haptic Feedback",
                    description = "Vibration feedback for interactions",
                    icon = Icons.Default.Vibration,
                    trailing = {
                        Switch(
                            checked = settings.hapticEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    HapticUtils.light(context)
                                }
                                viewModel.updateHapticEnabled(enabled)
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Effects
                SettingItem(
                    title = "Sound Effects",
                    description = "Audio feedback for game events",
                    icon = if (settings.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    trailing = {
                        Switch(
                            checked = settings.soundEnabled,
                            onCheckedChange = { enabled ->
                                HapticUtils.light(context)
                                viewModel.updateSoundEnabled(enabled)
                            }
                        )
                    }
                )
            }

            // Data Section
            SettingsSection(
                title = "Data",
                icon = Icons.Default.Storage
            ) {
                // Reset Statistics
                var showResetDialog by remember { mutableStateOf(false) }
                
                SettingItem(
                    title = "Reset Statistics",
                    description = "Clear all game statistics and history",
                    icon = Icons.Default.RestartAlt,
                    onClick = {
                        HapticUtils.impact(context)
                        showResetDialog = true
                    }
                )

                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("Reset Statistics") },
                        text = {
                            Text("Are you sure you want to reset all statistics? This action cannot be undone.")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    HapticUtils.success(context)
                                    viewModel.resetStatistics()
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

            // About Section
            SettingsSection(
                title = "About",
                icon = Icons.Default.Info
            ) {
                SettingItem(
                    title = "Version",
                    description = "1.0.0",
                    icon = Icons.Default.AppRegistration
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingItem(
                    title = "Developer",
                    description = "Enhanced Word Game by Clovis",
                    icon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingItem(
                    title = "Rate This App",
                    description = "Help us improve with your feedback",
                    icon = Icons.Default.Star,
                    onClick = {
                        HapticUtils.light(context)
                        // TODO: Open app store rating
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (onClick != null) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        },
        label = "container_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                trailing?.invoke()
            }
            
            content?.invoke(this)
        }
    }
}

@Composable
fun LazyHorizontalScrollableRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}
