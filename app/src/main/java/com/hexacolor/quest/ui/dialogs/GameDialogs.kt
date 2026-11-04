package com.hexacolor.quest.ui.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalPlay
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hexacolor.quest.R
import com.hexacolor.quest.model.BreakModalState
import com.hexacolor.quest.model.BreakType
import com.hexacolor.quest.model.RewardKind
import com.hexacolor.quest.ui.theme.QuestColors
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PauseDialog(
    levelNumber: Int,
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onLevelSelect: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit
) {
    Dialog(
        onDismissRequest = onResume,
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
                    .widthIn(max = 360.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(QuestColors.ModalTopGradient, QuestColors.ModalBottomGradient)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
                    .padding(24.dp)
                    .testTag("pause_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.pause_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.game_level_title, levelNumber),
                    style = MaterialTheme.typography.bodySmall,
                    color = QuestColors.MutedText,
                    modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
                )

                // Sound & Music Quick Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleSound,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pause_toggle_sound")
                    ) {
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
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.pause_sound),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (soundEnabled) Color.White else QuestColors.SubtleText
                        )
                    }

                    OutlinedButton(
                        onClick = onToggleMusic,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pause_toggle_music")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = if (musicEnabled) QuestColors.GoldStar else QuestColors.SubtleText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.pause_music),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (musicEnabled) Color.White else QuestColors.SubtleText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resume Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(QuestColors.EmeraldAccent, QuestColors.TealAccent)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, QuestColors.GlassWhiteBorder, RoundedCornerShape(16.dp))
                        .clickable(onClick = onResume)
                        .testTag("pause_resume_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.pause_resume),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onRestart,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pause_restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.pause_restart),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onLevelSelect,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pause_map_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_levels_map),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("pause_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = QuestColors.MutedText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.pause_settings),
                        style = MaterialTheme.typography.bodySmall,
                        color = QuestColors.MutedText
                    )
                }
            }
        }
    }
}

@Composable
fun VictoryDialog(
    levelNumber: Int,
    starsEarned: Int,
    score: Int,
    movesLeft: Int,
    coinsEarned: Int,
    isLastLevel: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onLevelSelect: () -> Unit
) {
    val confettiAnim = remember { Animatable(0f) }
    val particles = remember {
        List(45) {
            Triple(
                Random.nextFloat(), // x ratio
                (Random.nextFloat() * 2.0 * Math.PI).toFloat(), // angle
                QuestColors.ConfettiPalette[it % QuestColors.ConfettiPalette.size]
            )
        }
    }

    LaunchedEffect(Unit) {
        confettiAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1600, easing = LinearEasing)
        )
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(QuestColors.ScrimOverlay)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Celebratory Confetti Canvas Overlay
            if (confettiAnim.value < 1f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val p = confettiAnim.value
                    val alpha = (1f - p * 0.85f).coerceIn(0f, 1f)
                    particles.forEachIndexed { idx, (seedX, angle, color) ->
                        val startX = size.width * (0.2f + seedX * 0.6f)
                        val startY = size.height * 0.42f
                        val speed = 180f + (idx % 7) * 55f
                        val px = startX + cos(angle) * speed * p
                        val py = startY + sin(angle) * speed * p + (280f * p * p)
                        drawRect(
                            color = color.copy(alpha = alpha),
                            topLeft = Offset(px, py),
                            size = Size(10f, 6f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(QuestColors.VictoryTopGradient, QuestColors.ModalBottomGradient)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = QuestColors.GoldStar.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(24.dp)
                    .testTag("victory_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = QuestColors.GoldStar.copy(alpha = 0.16f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, QuestColors.GoldStar.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = QuestColors.GoldLight,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.victory_badge, levelNumber),
                            style = MaterialTheme.typography.labelMedium,
                            color = QuestColors.GoldLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.victory_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = QuestColors.GoldLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3 Animated Stars Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (starIndex in 1..3) {
                        val earned = starIndex <= starsEarned
                        val isMiddle = starIndex == 2
                        Box(
                            modifier = Modifier
                                .offset(y = if (isMiddle) (-8).dp else 0.dp)
                                .size(54.dp)
                                .background(
                                    brush = if (earned) {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                QuestColors.GoldLight,
                                                QuestColors.GoldStar,
                                                QuestColors.GoldDeep
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                QuestColors.FrostedHudStrong,
                                                QuestColors.ModalBottomGradient
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (earned) QuestColors.GoldLight else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (earned) Color.White else QuestColors.SubtleText,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3-Column Stats Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = QuestColors.FrostedHudStrong,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatColumn(
                            label = stringResource(R.string.game_score),
                            value = score.toString(),
                            valueColor = Color.White
                        )
                        StatColumn(
                            label = stringResource(R.string.game_moves_left),
                            value = "+$movesLeft",
                            valueColor = QuestColors.EmeraldLight
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.victory_coins).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = QuestColors.MutedText
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.MonetizationOn,
                                    contentDescription = null,
                                    tint = QuestColors.GoldStar,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+$coinsEarned",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = QuestColors.GoldStar,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isLastLevel) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.secondary,
                                        QuestColors.OrangeAccent,
                                        QuestColors.RoseVivid
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, QuestColors.GlassWhiteBorder, RoundedCornerShape(16.dp))
                            .clickable(onClick = onNextLevel)
                            .testTag("victory_next_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.victory_next_level),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReplay,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("victory_replay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.victory_replay),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onLevelSelect,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("victory_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.victory_map),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = QuestColors.MutedText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun GameOverDialog(
    levelNumber: Int,
    canUndo: Boolean,
    onRetry: () -> Unit,
    onUndo: () -> Unit,
    onExtraMoves: () -> Unit,
    onLevelSelect: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
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
                    .widthIn(max = 360.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(QuestColors.GameOverTopGradient, QuestColors.ModalBottomGradient)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = QuestColors.RoseVivid.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(24.dp)
                    .testTag("gameover_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = QuestColors.RoseVivid.copy(alpha = 0.16f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, QuestColors.RoseVivid.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = QuestColors.RoseAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.gameover_badge),
                            style = MaterialTheme.typography.labelMedium,
                            color = QuestColors.RoseAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.gameover_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Text(
                    text = stringResource(R.string.gameover_message, levelNumber),
                    style = MaterialTheme.typography.bodySmall,
                    color = QuestColors.MutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )

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
                        .border(1.dp, QuestColors.GlassWhiteBorder, RoundedCornerShape(16.dp))
                        .clickable(onClick = onExtraMoves)
                        .testTag("gameover_extra_moves_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalPlay,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.gameover_extra_moves),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("gameover_retry_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.gameover_retry),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (canUndo) {
                        OutlinedButton(
                            onClick = onUndo,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("gameover_undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.game_action_undo),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onLevelSelect,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("gameover_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.victory_map),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardBreakDialog(
    state: BreakModalState,
    onCompleteReward: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!state.isOpen) return

    val totalSeconds = if (state.type == BreakType.REWARDED) 5 else 3
    var secondsLeft by remember(state) { mutableIntStateOf(totalSeconds) }

    LaunchedEffect(state) {
        secondsLeft = totalSeconds
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }

    val unlocked = secondsLeft == 0
    val handleFinish = {
        if (state.type == BreakType.REWARDED && unlocked) {
            onCompleteReward()
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { if (unlocked) handleFinish() },
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
                    .widthIn(max = 360.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
                    .padding(22.dp)
                    .testTag("reward_break_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = QuestColors.GoldStar.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, QuestColors.GoldStar.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = stringResource(R.string.reward_header_badge).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = QuestColors.GoldLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (unlocked) {
                        IconButton(
                            onClick = handleFinish,
                            modifier = Modifier.testTag("reward_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_close_dialog),
                                tint = Color.White
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.reward_skip_countdown, secondsLeft),
                            style = MaterialTheme.typography.labelSmall,
                            color = QuestColors.MutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = QuestColors.IndigoContainer,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, QuestColors.IndigoAccent.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tv,
                            contentDescription = null,
                            tint = QuestColors.IndigoAccent,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.type == BreakType.REWARDED) {
                                stringResource(R.string.reward_title_supporter)
                            } else {
                                stringResource(R.string.reward_title_break)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.type == BreakType.REWARDED) {
                                stringResource(R.string.reward_desc_supporter)
                            } else {
                                stringResource(R.string.reward_desc_break)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = {
                                ((totalSeconds - secondsLeft).toFloat() / totalSeconds.toFloat())
                                    .coerceIn(0f, 1f)
                            },
                            color = QuestColors.CyanGlow,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (state.type == BreakType.REWARDED) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (unlocked) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = QuestColors.EmeraldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.rewardKind == RewardKind.EXTRA_MOVES) {
                                    stringResource(R.string.reward_unlocked_moves)
                                } else {
                                    stringResource(R.string.reward_unlocked_hint)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = QuestColors.EmeraldLight,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.reward_watch_prompt),
                                style = MaterialTheme.typography.bodySmall,
                                color = QuestColors.MutedText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Button(
                    onClick = handleFinish,
                    enabled = unlocked,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QuestColors.EmeraldAccent,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reward_claim_button")
                ) {
                    Text(
                        text = if (unlocked) {
                            stringResource(R.string.reward_claim)
                        } else {
                            stringResource(R.string.reward_please_wait, secondsLeft)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
