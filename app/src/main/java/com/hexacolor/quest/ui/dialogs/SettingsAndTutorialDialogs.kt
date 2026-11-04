package com.hexacolor.quest.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hexacolor.quest.R
import com.hexacolor.quest.model.TileTier
import com.hexacolor.quest.ui.components.HexTileGraphic
import com.hexacolor.quest.ui.theme.QuestColors
import kotlin.math.roundToInt

@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    hapticsEnabled: Boolean,
    soundVolume: Float,
    musicVolume: Float,
    onClose: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleHaptics: () -> Unit,
    onChangeSoundVolume: (Float) -> Unit,
    onChangeMusicVolume: (Float) -> Unit,
    onResetProgress: () -> Unit
) {
    var confirmReset by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(QuestColors.ScrimOverlay)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(QuestColors.ModalTopGradient, QuestColors.ModalBottomGradient)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("settings_dialog")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("settings_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_close_dialog),
                            tint = QuestColors.MutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sound FX Section
                Surface(
                    color = QuestColors.FrostedHudStrong,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (soundEnabled) {
                                        Icons.AutoMirrored.Filled.VolumeUp
                                    } else {
                                        Icons.AutoMirrored.Filled.VolumeOff
                                    },
                                    contentDescription = null,
                                    tint = if (soundEnabled) QuestColors.EmeraldLight else QuestColors.SubtleText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_sound_fx),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { onToggleSound() },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = QuestColors.EmeraldAccent
                                ),
                                modifier = Modifier.testTag("settings_sound_switch")
                            )
                        }

                        if (soundEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Slider(
                                    value = soundVolume,
                                    onValueChange = onChangeSoundVolume,
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = QuestColors.EmeraldLight,
                                        activeTrackColor = QuestColors.EmeraldAccent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_sound_slider")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${(soundVolume * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = QuestColors.MutedText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Background Music Section
                Surface(
                    color = QuestColors.FrostedHudStrong,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = if (musicEnabled) QuestColors.GoldStar else QuestColors.SubtleText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_music),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Switch(
                                checked = musicEnabled,
                                onCheckedChange = { onToggleMusic() },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.testTag("settings_music_switch")
                            )
                        }

                        if (musicEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Slider(
                                    value = musicVolume,
                                    onValueChange = onChangeMusicVolume,
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = QuestColors.GoldLight,
                                        activeTrackColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_music_slider")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${(musicVolume * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = QuestColors.MutedText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tactile Haptics Section
                Surface(
                    color = QuestColors.FrostedHudStrong,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Smartphone,
                                contentDescription = null,
                                tint = QuestColors.CyanGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_haptics),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = { onToggleHaptics() },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = QuestColors.CyanGlow
                            ),
                            modifier = Modifier.testTag("settings_haptics_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Offline & Privacy Note
                Surface(
                    color = QuestColors.FrostedHudCard,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = QuestColors.EmeraldLight,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.settings_privacy_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = QuestColors.MutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))

                if (!confirmReset) {
                    TextButton(
                        onClick = { confirmReset = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("settings_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = null,
                            tint = QuestColors.RoseAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.settings_reset_progress),
                            style = MaterialTheme.typography.labelLarge,
                            color = QuestColors.RoseAccent
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onResetProgress()
                                confirmReset = false
                                onClose()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("settings_confirm_reset_button")
                        ) {
                            Text(
                                text = stringResource(R.string.settings_confirm_reset),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = { confirmReset = false },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("settings_cancel_reset_button")
                        ) {
                            Text(
                                text = stringResource(R.string.settings_cancel),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialDialog(
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(QuestColors.ScrimOverlay)
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .heightIn(max = 640.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(QuestColors.ModalTopGradient, QuestColors.ModalBottomGradient)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
                    .padding(22.dp)
                    .testTag("tutorial_dialog")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = QuestColors.CyanGlow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.tutorial_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("tutorial_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_close_dialog),
                            tint = QuestColors.MutedText
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TutorialStepCard(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        accentColor = QuestColors.CyanGlow,
                        title = stringResource(R.string.tutorial_step1_title),
                        body = stringResource(R.string.tutorial_step1_body)
                    )
                    TutorialStepCard(
                        icon = Icons.Filled.Layers,
                        accentColor = QuestColors.EmeraldLight,
                        title = stringResource(R.string.tutorial_step2_title),
                        body = stringResource(R.string.tutorial_step2_body)
                    )
                    TutorialStepCard(
                        icon = Icons.Filled.AutoAwesome,
                        accentColor = QuestColors.GoldStar,
                        title = stringResource(R.string.tutorial_step3_title),
                        body = stringResource(R.string.tutorial_step3_body)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.tutorial_hierarchy_header),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    val tiers = TileTier.entries
                    for (rowIdx in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (colIdx in 0..1) {
                                val tier = tiers[rowIdx * 2 + colIdx]
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = QuestColors.FrostedHudStrong,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HexTileGraphic(
                                            tier = tier,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "T${tier.tierNumber} ${tier.shortName}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "+${tier.scoreValue} pts",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = QuestColors.MutedText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(QuestColors.CyanGlow, QuestColors.BlueVivid)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(onClick = onClose)
                        .testTag("tutorial_got_it_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.tutorial_got_it),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialStepCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = QuestColors.FrostedHudStrong,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = QuestColors.MutedText
            )
        }
    }
}
