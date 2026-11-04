package com.hexacolor.quest.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hexacolor.quest.R
import com.hexacolor.quest.data.LevelRepository
import com.hexacolor.quest.model.LevelConfig
import com.hexacolor.quest.model.UserProgress
import com.hexacolor.quest.ui.components.DifficultyPill
import com.hexacolor.quest.ui.components.OceanBackdrop
import com.hexacolor.quest.ui.theme.QuestColors

@Composable
fun LevelSelectScreen(
    progress: UserProgress,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit,
    onPlayClick: () -> Unit
) {
    val initialChapter = ((progress.unlockedLevel - 1) / 20 + 1).coerceIn(1, 5)
    var selectedChapterId by remember(initialChapter) { mutableIntStateOf(initialChapter) }

    val chapters = LevelRepository.CHAPTERS
    val currentChapter = chapters.firstOrNull { it.id == selectedChapterId } ?: chapters.first()
    val chapterLevels = remember(selectedChapterId) {
        LevelRepository.getAllLevels().subList(
            currentChapter.startLevel - 1,
            currentChapter.endLevel
        )
    }

    OceanBackdrop {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val columns = when {
                maxWidth >= 720.dp -> 5
                else -> 4
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 820.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = {
                                onPlayClick()
                                onBack()
                            },
                            color = QuestColors.FrostedHudCard,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(44.dp)
                                .testTag("levels_back_button")
                                .semantics {
                                    contentDescription = "Back to home"
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back),
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.home_levels_map),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${currentChapter.name} · ${currentChapter.tierInfo}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Total Stars Pill
                    Surface(
                        color = QuestColors.FrostedHudCard,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
                                text = " / 300",
                                style = MaterialTheme.typography.labelSmall,
                                color = QuestColors.MutedText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5 Chapter Tabs
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = QuestColors.FrostedHudCard,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        chapters.forEach { chapter ->
                            val isActive = chapter.id == selectedChapterId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .then(
                                        if (isActive) {
                                            Modifier
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(
                                                            QuestColors.RoseVivid,
                                                            MaterialTheme.colorScheme.secondary
                                                        )
                                                    ),
                                                    shape = RoundedCornerShape(13.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    QuestColors.GlassWhiteBorder,
                                                    RoundedCornerShape(13.dp)
                                                )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable {
                                        onPlayClick()
                                        selectedChapterId = chapter.id
                                    }
                                    .padding(vertical = 7.dp)
                                    .testTag("chapter_tab_${chapter.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Ch.${chapter.id}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isActive) Color.White else QuestColors.MutedText,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = chapter.range,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isActive) QuestColors.GoldLight else QuestColors.SubtleText
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Levels Grid (20 levels per chapter)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("levels_grid")
                ) {
                    items(chapterLevels, key = { it.id }) { level ->
                        val isUnlocked = level.id <= progress.unlockedLevel
                        val isCurrent = level.id == progress.unlockedLevel
                        val starsEarned = progress.stars[level.id] ?: 0
                        val highScore = progress.highScores[level.id] ?: 0

                        LevelGridCard(
                            level = level,
                            isUnlocked = isUnlocked,
                            isCurrent = isCurrent,
                            starsEarned = starsEarned,
                            highScore = highScore,
                            onClick = {
                                onPlayClick()
                                onSelectLevel(level.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelGridCard(
    level: LevelConfig,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    starsEarned: Int,
    highScore: Int,
    onClick: () -> Unit
) {
    val borderColor = when {
        isCurrent -> QuestColors.CyanGlow
        isUnlocked -> QuestColors.GlassWhiteBorder
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        onClick = onClick,
        enabled = isUnlocked,
        color = if (isUnlocked) QuestColors.FrostedHudStrong else QuestColors.FrostedHudCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isCurrent) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .alpha(if (isUnlocked) 1f else 0.58f)
            .minimumInteractiveComponentSize()
            .testTag("level_card_${level.id}")
            .semantics {
                contentDescription = if (isUnlocked) {
                    "Level ${level.id}, ${level.difficulty}, $starsEarned stars"
                } else {
                    "Level ${level.id} locked"
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${level.id}",
                    style = MaterialTheme.typography.titleSmall,
                    color = when {
                        isCurrent -> QuestColors.CyanGlow
                        isUnlocked -> Color.White
                        else -> QuestColors.SubtleText
                    },
                    fontWeight = FontWeight.Black
                )
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(QuestColors.CyanGlow, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isUnlocked) {
                DifficultyPill(difficulty = level.difficulty)
            } else {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = QuestColors.SubtleText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (s in 1..3) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (isUnlocked && s <= starsEarned) {
                            QuestColors.GoldStar
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            if (isUnlocked && highScore > 0) {
                Text(
                    text = "$highScore",
                    style = MaterialTheme.typography.labelSmall,
                    color = QuestColors.MutedText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
