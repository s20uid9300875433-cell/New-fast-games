package com.hexacolor.quest.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hexacolor.quest.R
import com.hexacolor.quest.data.LevelRepository
import com.hexacolor.quest.model.UserProgress
import com.hexacolor.quest.ui.components.DifficultyPill
import com.hexacolor.quest.ui.components.HeroCrownHexLogo
import com.hexacolor.quest.ui.components.OceanBackdrop
import com.hexacolor.quest.ui.dialogs.SettingsDialog
import com.hexacolor.quest.ui.dialogs.TutorialDialog
import com.hexacolor.quest.ui.theme.QuestColors
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    progress: UserProgress,
    onPlayCurrentLevel: () -> Unit,
    onOpenLevelSelect: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleHaptics: () -> Unit,
    onChangeSoundVolume: (Float) -> Unit,
    onChangeMusicVolume: (Float) -> Unit,
    onResetProgress: () -> Unit,
    onPlayClick: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }

    val currentLevelConfig = remember(progress.unlockedLevel) {
        LevelRepository.getLevel(progress.unlockedLevel)
    }
    val progressPercent = (((progress.unlockedLevel - 1) / 100f) * 100f)
        .roundToInt()
        .coerceIn(0, 100)

    OceanBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status & Quick Audio Toggles Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 540.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stars Badge
                    Surface(
                        color = QuestColors.FrostedHudCard,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder),
                        modifier = Modifier.testTag("home_stars_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = QuestColors.GoldStar,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${progress.totalStars}",
                                style = MaterialTheme.typography.labelLarge,
                                color = QuestColors.GoldLight,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "/300",
                                style = MaterialTheme.typography.labelSmall,
                                color = QuestColors.MutedText
                            )
                        }
                    }

                    // Coins Badge
                    Surface(
                        color = QuestColors.FrostedHudCard,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder),
                        modifier = Modifier.testTag("home_coins_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MonetizationOn,
                                contentDescription = null,
                                tint = QuestColors.GoldStar,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${progress.coins}",
                                style = MaterialTheme.typography.labelLarge,
                                color = QuestColors.GoldLight,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Audio & Settings Icon Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickTopIconButton(
                        onClick = onToggleSound,
                        contentDesc = stringResource(R.string.cd_toggle_sound),
                        testTag = "home_toggle_sound_button"
                    ) {
                        Icon(
                            imageVector = if (progress.soundEnabled) {
                                Icons.AutoMirrored.Filled.VolumeUp
                            } else {
                                Icons.AutoMirrored.Filled.VolumeOff
                            },
                            contentDescription = null,
                            tint = if (progress.soundEnabled) {
                                QuestColors.EmeraldLight
                            } else {
                                QuestColors.SubtleText
                            },
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    QuickTopIconButton(
                        onClick = onToggleMusic,
                        contentDesc = stringResource(R.string.cd_toggle_music),
                        testTag = "home_toggle_music_button"
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = if (progress.musicEnabled) {
                                QuestColors.GoldStar
                            } else {
                                QuestColors.SubtleText
                            },
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    QuickTopIconButton(
                        onClick = {
                            onPlayClick()
                            showSettings = true
                        },
                        contentDesc = stringResource(R.string.cd_open_settings),
                        testTag = "home_settings_button"
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Center Brand Hero Lockup + Level Progress Card
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HeroCrownHexLogo()

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall.merge(
                        TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    QuestColors.RoseAccent,
                                    QuestColors.GoldLight,
                                    QuestColors.CyanGlow
                                )
                            )
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Level Progress Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_progress_card"),
                    color = QuestColors.FrostedHudStrong,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = QuestColors.CyanGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(
                                        R.string.home_level_of_total,
                                        progress.unlockedLevel
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                            }
                            DifficultyPill(difficulty = currentLevelConfig.difficulty)
                        }

                        // Gradient Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val barFraction = (progressPercent / 100f).coerceAtLeast(0.05f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barFraction)
                                    .fillMaxHeight()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                QuestColors.CyanGlow,
                                                QuestColors.BlueVivid,
                                                QuestColors.RoseVivid
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.home_progress_percent, progressPercent),
                                style = MaterialTheme.typography.labelSmall,
                                color = QuestColors.MutedText
                            )
                            Text(
                                text = currentLevelConfig.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = QuestColors.MutedText
                            )
                        }
                    }
                }
            }

            // Bottom Primary & Secondary Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    QuestColors.RoseVivid,
                                    MaterialTheme.colorScheme.secondary,
                                    QuestColors.EmeraldAccent
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(1.dp, QuestColors.GlassWhiteBorder, RoundedCornerShape(18.dp))
                        .clickable {
                            onPlayClick()
                            onPlayCurrentLevel()
                        }
                        .testTag("home_play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (progress.unlockedLevel > 1) {
                                stringResource(R.string.home_continue_level, progress.unlockedLevel)
                            } else {
                                stringResource(R.string.home_start_quest)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = {
                            onPlayClick()
                            onOpenLevelSelect()
                        },
                        color = QuestColors.FrostedHudStrong,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("home_levels_map_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GridView,
                                contentDescription = null,
                                tint = QuestColors.CyanGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.home_levels_map),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            onPlayClick()
                            showTutorial = true
                        },
                        color = QuestColors.FrostedHudStrong,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("home_how_to_play_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = QuestColors.EmeraldLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.home_how_to_play),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        if (showSettings) {
            SettingsDialog(
                soundEnabled = progress.soundEnabled,
                musicEnabled = progress.musicEnabled,
                hapticsEnabled = progress.hapticsEnabled,
                soundVolume = progress.soundVolume,
                musicVolume = progress.musicVolume,
                onClose = {
                    onPlayClick()
                    showSettings = false
                },
                onToggleSound = onToggleSound,
                onToggleMusic = onToggleMusic,
                onToggleHaptics = onToggleHaptics,
                onChangeSoundVolume = onChangeSoundVolume,
                onChangeMusicVolume = onChangeMusicVolume,
                onResetProgress = onResetProgress
            )
        }

        if (showTutorial) {
            TutorialDialog(
                onClose = {
                    onPlayClick()
                    showTutorial = false
                }
            )
        }
    }
}

@Composable
private fun QuickTopIconButton(
    onClick: () -> Unit,
    contentDesc: String,
    testTag: String,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        color = QuestColors.FrostedHudStrong,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .testTag(testTag)
            .semantics { contentDescription = contentDesc }
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
