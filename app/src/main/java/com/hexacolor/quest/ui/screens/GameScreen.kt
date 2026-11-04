package com.hexacolor.quest.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hexacolor.quest.R
import com.hexacolor.quest.model.ObjectiveType
import com.hexacolor.quest.model.TileTier
import com.hexacolor.quest.model.UserProgress
import com.hexacolor.quest.ui.components.BlockTray
import com.hexacolor.quest.ui.components.BoardGeometry
import com.hexacolor.quest.ui.components.DifficultyPill
import com.hexacolor.quest.ui.components.HexTileGraphic
import com.hexacolor.quest.ui.components.HexagonBoard
import com.hexacolor.quest.ui.components.OceanBackdrop
import com.hexacolor.quest.ui.dialogs.GameOverDialog
import com.hexacolor.quest.ui.dialogs.PauseDialog
import com.hexacolor.quest.ui.dialogs.RewardBreakDialog
import com.hexacolor.quest.ui.dialogs.SettingsDialog
import com.hexacolor.quest.ui.dialogs.VictoryDialog
import com.hexacolor.quest.ui.theme.QuestColors
import com.hexacolor.quest.viewmodel.GameUiState
import com.hexacolor.quest.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    levelId: Int,
    uiState: GameUiState,
    progress: UserProgress,
    gameViewModel: GameViewModel,
    onExitToLevelSelect: () -> Unit,
    onAdvanceToLevel: (Int) -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleHaptics: () -> Unit,
    onChangeSoundVolume: (Float) -> Unit,
    onChangeMusicVolume: (Float) -> Unit,
    onResetProgress: () -> Unit
) {
    LaunchedEffect(levelId) {
        gameViewModel.loadLevel(levelId)
    }

    var showSettings by remember { mutableStateOf(false) }

    BackHandler(
        enabled = !uiState.isPaused &&
            !uiState.isVictory &&
            !uiState.isGameOver &&
            !uiState.breakModalState.isOpen &&
            !showSettings
    ) {
        gameViewModel.setPaused(true)
    }
    var boardGeometry by remember { mutableStateOf(BoardGeometry()) }
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    OceanBackdrop(
        modifier = Modifier.onGloballyPositioned { coords ->
            rootOrigin = coords.positionInRoot()
        }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val isWideScreen = maxWidth >= 680.dp && maxWidth > maxHeight

            if (isWideScreen) {
                // Canonical Two-Pane Supporting Layout for Tablets / Foldables / Landscape
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 1040.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        HexagonBoard(
                            radius = uiState.levelConfig.radius,
                            blockedCoords = uiState.levelConfig.blockedCoords,
                            tiles = uiState.tiles,
                            selectedCoord = uiState.selectedBoardCoord,
                            validTargets = uiState.validTargets,
                            hint = uiState.activeHint,
                            isMerging = uiState.isMerging,
                            shakeTrigger = uiState.shakeTrigger,
                            sparkles = uiState.sparkles,
                            hoverDropCoord = uiState.hoverDropCoord,
                            previewTier = uiState.activePreviewTier,
                            onCellTapped = gameViewModel::onBoardCellTapped,
                            onGeometryUpdated = { boardGeometry = it },
                            modifier = Modifier.fillMaxHeight(0.94f)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        GameHudHeader(
                            uiState = uiState,
                            onPause = { gameViewModel.setPaused(true) },
                            onRestart = gameViewModel::restartLevel,
                            onDismissTip = gameViewModel::dismissTip
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            BlockTray(
                                blocks = uiState.trayBlocks,
                                selectedBlockId = uiState.selectedTrayBlockId,
                                draggingBlockId = uiState.draggingBlock?.id,
                                disabled = uiState.isMerging || uiState.isVictory || uiState.isGameOver,
                                onSelectBlock = gameViewModel::onSelectTrayBlock,
                                onDragStart = gameViewModel::onTrayDragStart,
                                onDragMove = { rootPt ->
                                    val candidate = boardGeometry.rootOffsetToHex(rootPt)
                                    gameViewModel.onTrayDragMove(rootPt, candidate)
                                },
                                onDragEnd = { endRootPt ->
                                    val overBoard = boardGeometry.rootBounds.contains(endRootPt)
                                    gameViewModel.onTrayDragEnd(overBoard)
                                }
                            )

                            GameActionToolbar(
                                canUndo = uiState.canUndo,
                                isMerging = uiState.isMerging,
                                hintsRemaining = progress.hintsRemaining,
                                onUndo = gameViewModel::undoMove,
                                onHint = { gameViewModel.requestHint(progress.hintsRemaining) },
                                onRestart = gameViewModel::restartLevel
                            )
                        }
                    }
                }
            } else {
                // Handheld Vertical Stack Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 500.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    GameHudHeader(
                        uiState = uiState,
                        onPause = { gameViewModel.setPaused(true) },
                        onRestart = gameViewModel::restartLevel,
                        onDismissTip = gameViewModel::dismissTip
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HexagonBoard(
                            radius = uiState.levelConfig.radius,
                            blockedCoords = uiState.levelConfig.blockedCoords,
                            tiles = uiState.tiles,
                            selectedCoord = uiState.selectedBoardCoord,
                            validTargets = uiState.validTargets,
                            hint = uiState.activeHint,
                            isMerging = uiState.isMerging,
                            shakeTrigger = uiState.shakeTrigger,
                            sparkles = uiState.sparkles,
                            hoverDropCoord = uiState.hoverDropCoord,
                            previewTier = uiState.activePreviewTier,
                            onCellTapped = gameViewModel::onBoardCellTapped,
                            onGeometryUpdated = { boardGeometry = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BlockTray(
                            blocks = uiState.trayBlocks,
                            selectedBlockId = uiState.selectedTrayBlockId,
                            draggingBlockId = uiState.draggingBlock?.id,
                            disabled = uiState.isMerging || uiState.isVictory || uiState.isGameOver,
                            onSelectBlock = gameViewModel::onSelectTrayBlock,
                            onDragStart = gameViewModel::onTrayDragStart,
                            onDragMove = { rootPt ->
                                val candidate = boardGeometry.rootOffsetToHex(rootPt)
                                gameViewModel.onTrayDragMove(rootPt, candidate)
                            },
                            onDragEnd = { endRootPt ->
                                val overBoard = boardGeometry.rootBounds.contains(endRootPt)
                                gameViewModel.onTrayDragEnd(overBoard)
                            }
                        )

                        GameActionToolbar(
                            canUndo = uiState.canUndo,
                            isMerging = uiState.isMerging,
                            hintsRemaining = progress.hintsRemaining,
                            onUndo = gameViewModel::undoMove,
                            onHint = { gameViewModel.requestHint(progress.hintsRemaining) },
                            onRestart = gameViewModel::restartLevel
                        )
                    }
                }
            }
        }

        // Floating 2.5D Hexagon Drag-and-Drop Proxy
        val draggingBlock = uiState.draggingBlock
        val dragRootPt = uiState.dragRootOffset
        if (draggingBlock != null && dragRootPt != null) {
            val proxySizeDp = 68.dp
            val proxyHalfPx = with(density) { (proxySizeDp / 2).toPx() }
            val fingerLiftPx = with(density) { 20.dp.toPx() }
            val localPt = dragRootPt - rootOrigin

            HexTileGraphic(
                tier = draggingBlock.tier,
                modifier = Modifier
                    .size(proxySizeDp)
                    .offset {
                        IntOffset(
                            x = (localPt.x - proxyHalfPx).roundToInt(),
                            y = (localPt.y - proxyHalfPx - fingerLiftPx).roundToInt()
                        )
                    }
            )
        }

        // Modals
        if (uiState.isPaused) {
            PauseDialog(
                levelNumber = uiState.levelConfig.id,
                soundEnabled = progress.soundEnabled,
                musicEnabled = progress.musicEnabled,
                onResume = { gameViewModel.setPaused(false) },
                onRestart = {
                    gameViewModel.setPaused(false)
                    gameViewModel.restartLevel()
                },
                onLevelSelect = {
                    gameViewModel.setPaused(false)
                    onExitToLevelSelect()
                },
                onOpenSettings = { showSettings = true },
                onToggleSound = onToggleSound,
                onToggleMusic = onToggleMusic
            )
        }

        if (uiState.isVictory) {
            VictoryDialog(
                levelNumber = uiState.levelConfig.id,
                starsEarned = uiState.starsEarned,
                score = uiState.score,
                movesLeft = uiState.movesLeft,
                coinsEarned = uiState.coinsEarned,
                isLastLevel = uiState.levelConfig.id >= 100,
                onNextLevel = {
                    gameViewModel.onNextLevelClicked(
                        onAdvanceToLevel = onAdvanceToLevel,
                        onExitToMap = onExitToLevelSelect
                    )
                },
                onReplay = gameViewModel::restartLevel,
                onLevelSelect = onExitToLevelSelect
            )
        }

        if (uiState.isGameOver) {
            GameOverDialog(
                levelNumber = uiState.levelConfig.id,
                canUndo = uiState.canUndo,
                onRetry = gameViewModel::restartLevel,
                onUndo = gameViewModel::undoMove,
                onExtraMoves = gameViewModel::requestExtraMovesBreak,
                onLevelSelect = onExitToLevelSelect
            )
        }

        if (showSettings) {
            SettingsDialog(
                soundEnabled = progress.soundEnabled,
                musicEnabled = progress.musicEnabled,
                hapticsEnabled = progress.hapticsEnabled,
                soundVolume = progress.soundVolume,
                musicVolume = progress.musicVolume,
                onClose = { showSettings = false },
                onToggleSound = onToggleSound,
                onToggleMusic = onToggleMusic,
                onToggleHaptics = onToggleHaptics,
                onChangeSoundVolume = onChangeSoundVolume,
                onChangeMusicVolume = onChangeMusicVolume,
                onResetProgress = onResetProgress
            )
        }

        RewardBreakDialog(
            state = uiState.breakModalState,
            onCompleteReward = gameViewModel::completeBreakReward,
            onDismiss = gameViewModel::dismissBreakModal
        )
    }
}

@Composable
private fun GameHudHeader(
    uiState: GameUiState,
    onPause: () -> Unit,
    onRestart: () -> Unit,
    onDismissTip: () -> Unit
) {
    val obj = uiState.levelConfig.objective
    val progressLabel = when (obj.type) {
        ObjectiveType.REACH_TIER -> {
            val target = obj.targetTier ?: TileTier.TIER_3
            "Current: T${uiState.highestTier.tierNumber} / Goal: T${target.tierNumber} (${target.tierName})"
        }
        ObjectiveType.MERGE_COUNT -> "Merges: ${uiState.mergesCount} / ${obj.count ?: 1}"
        ObjectiveType.TARGET_SCORE -> "Score: ${uiState.score} / ${obj.targetScore ?: 500}"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Top Row: Pause, Level Title & Difficulty, Restart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onPause,
                color = QuestColors.FrostedHudCard,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(44.dp)
                    .testTag("game_pause_button")
                    .semantics { contentDescription = "Pause game" }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = stringResource(R.string.cd_pause_game),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                color = QuestColors.FrostedHudCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.game_level_title, uiState.levelConfig.id),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        DifficultyPill(difficulty = uiState.levelConfig.difficulty)
                    }
                    Text(
                        text = uiState.levelConfig.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Surface(
                onClick = onRestart,
                color = QuestColors.FrostedHudCard,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(44.dp)
                    .testTag("game_top_restart_button")
                    .semantics { contentDescription = "Restart level" }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.cd_restart_level),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Moves Left & Score Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val lowMoves = uiState.movesLeft <= 3
            Surface(
                color = if (lowMoves) QuestColors.RoseContainer else QuestColors.FrostedHudCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (lowMoves) QuestColors.RoseAccent else QuestColors.GlassWhiteBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("game_moves_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.game_moves_left).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (lowMoves) QuestColors.RoseAccent else QuestColors.MutedText
                    )
                    Text(
                        text = "${uiState.movesLeft}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Surface(
                color = QuestColors.FrostedHudCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder),
                modifier = Modifier
                    .weight(1f)
                    .testTag("game_score_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.game_score).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = QuestColors.MutedText
                    )
                    Text(
                        text = "${uiState.score}",
                        style = MaterialTheme.typography.titleMedium,
                        color = QuestColors.GoldLight,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Objective Card + Live Star Threshold Preview
        Surface(
            color = QuestColors.FrostedHudStrong,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("game_objective_card")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                color = QuestColors.CyanGlow.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = QuestColors.CyanGlow.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GpsFixed,
                            contentDescription = null,
                            tint = QuestColors.CyanGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = obj.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = progressLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val th = uiState.levelConfig.starThresholds
                    for (starNum in 1..3) {
                        val req = when (starNum) {
                            3 -> th.threeStars
                            2 -> th.twoStars
                            else -> 0
                        }
                        val active = uiState.movesLeft >= req
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = if (active) QuestColors.GoldStar else QuestColors.SubtleText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Dismissible Tutorial Tip Banner (Levels 1-5) + Active Hint Explanation
        val bannerText = uiState.activeHint?.explanation
            ?: uiState.levelConfig.tutorialTip?.takeIf { !uiState.tipDismissed }

        if (bannerText != null) {
            Surface(
                color = QuestColors.FrostedHudStrong,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, QuestColors.CyanGlow.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_tip_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = QuestColors.CyanGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bannerText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (uiState.activeHint == null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            onClick = onDismissTip,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, QuestColors.CyanGlow.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("game_dismiss_tip_button")
                        ) {
                            Text(
                                text = stringResource(R.string.game_tip_ok),
                                style = MaterialTheme.typography.labelMedium,
                                color = QuestColors.CyanGlow,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameActionToolbar(
    canUndo: Boolean,
    isMerging: Boolean,
    hintsRemaining: Int,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo Button
        Surface(
            onClick = onUndo,
            enabled = canUndo && !isMerging,
            color = QuestColors.FrostedHudStrong,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .alpha(if (canUndo && !isMerging) 1f else 0.45f)
                .testTag("game_undo_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.game_action_undo),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }

        // Hint Button
        Box(
            modifier = Modifier
                .weight(1.15f)
                .height(48.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(QuestColors.GoldStar, MaterialTheme.colorScheme.secondary)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(enabled = !isMerging, onClick = onHint)
                .testTag("game_hint_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.game_action_hint),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(5.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.22f),
                    shape = CircleShape
                ) {
                    Text(
                        text = if (hintsRemaining > 0) {
                            "$hintsRemaining"
                        } else {
                            stringResource(R.string.game_hint_bonus)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Restart Button
        Surface(
            onClick = onRestart,
            color = QuestColors.FrostedHudStrong,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("game_restart_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.game_action_restart),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}
