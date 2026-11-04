package com.hexacolor.quest.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hexacolor.quest.audio.SoundSynthesizer
import com.hexacolor.quest.data.LevelRepository
import com.hexacolor.quest.data.ProgressRepository
import com.hexacolor.quest.logic.GameEngine
import com.hexacolor.quest.logic.HintSolver
import com.hexacolor.quest.logic.MoveResult
import com.hexacolor.quest.model.BoardTile
import com.hexacolor.quest.model.BreakModalState
import com.hexacolor.quest.model.BreakType
import com.hexacolor.quest.model.HexCoord
import com.hexacolor.quest.model.HintSourceType
import com.hexacolor.quest.model.HintSuggestion
import com.hexacolor.quest.model.LevelConfig
import com.hexacolor.quest.model.RewardKind
import com.hexacolor.quest.model.SparkleParticle
import com.hexacolor.quest.model.TileTier
import com.hexacolor.quest.model.TrayBlock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.random.Random

data class GameUiState(
    val levelConfig: LevelConfig = LevelRepository.getLevel(1),
    val tiles: List<BoardTile> = emptyList(),
    val trayBlocks: List<TrayBlock> = emptyList(),
    val selectedBoardCoord: HexCoord? = null,
    val selectedTrayBlockId: String? = null,
    val validTargets: List<HexCoord> = emptyList(),
    val movesLeft: Int = 12,
    val score: Int = 0,
    val mergesCount: Int = 0,
    val highestTier: TileTier = TileTier.TIER_1,
    val canUndo: Boolean = false,
    val activeHint: HintSuggestion? = null,
    val isMerging: Boolean = false,
    val shakeTrigger: Int = 0,
    val sparkles: List<SparkleParticle> = emptyList(),
    val tipDismissed: Boolean = false,
    val isPaused: Boolean = false,
    val isVictory: Boolean = false,
    val isGameOver: Boolean = false,
    val starsEarned: Int = 0,
    val coinsEarned: Int = 0,
    val draggingBlock: TrayBlock? = null,
    val dragRootOffset: Offset? = null,
    val hoverDropCoord: HexCoord? = null,
    val breakModalState: BreakModalState = BreakModalState()
) {
    val activePreviewTier: TileTier?
        get() = draggingBlock?.tier
            ?: selectedTrayBlockId?.let { id -> trayBlocks.firstOrNull { it.id == id }?.tier }
}

class GameViewModel(
    private val progressRepository: ProgressRepository,
    private val soundSynthesizer: SoundSynthesizer
) : ViewModel() {

    private var engine: GameEngine = GameEngine(LevelRepository.getLevel(1))
    private var levelsCompletedSinceBreak: Int = 0
    private var pendingBreakDismissAction: (() -> Unit)? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun loadLevel(levelId: Int) {
        val config = LevelRepository.getLevel(levelId)
        engine = GameEngine(config)
        _uiState.value = GameUiState(
            levelConfig = config,
            tiles = engine.getTiles(),
            trayBlocks = engine.trayBlocks,
            movesLeft = engine.movesLeft,
            score = engine.score,
            mergesCount = engine.mergesCount,
            highestTier = engine.highestTier,
            canUndo = engine.canUndo
        )
    }

    fun restartLevel() {
        soundSynthesizer.playClick()
        engine.reset()
        _uiState.update { state ->
            GameUiState(
                levelConfig = state.levelConfig,
                tiles = engine.getTiles(),
                trayBlocks = engine.trayBlocks,
                movesLeft = engine.movesLeft,
                score = engine.score,
                mergesCount = engine.mergesCount,
                highestTier = engine.highestTier,
                canUndo = engine.canUndo
            )
        }
    }

    fun setPaused(paused: Boolean) {
        soundSynthesizer.playClick()
        _uiState.update { it.copy(isPaused = paused) }
    }

    fun dismissTip() {
        soundSynthesizer.playClick()
        _uiState.update { it.copy(tipDismissed = true) }
    }

    fun onSelectTrayBlock(blockId: String) {
        val current = _uiState.value
        if (current.isMerging || current.isVictory || current.isGameOver) return
        val block = current.trayBlocks.firstOrNull { it.id == blockId && !it.isUsed } ?: return

        soundSynthesizer.playSelect()
        if (current.selectedTrayBlockId == block.id) {
            _uiState.update {
                it.copy(
                    selectedTrayBlockId = null,
                    validTargets = emptyList()
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedTrayBlockId = block.id,
                    selectedBoardCoord = null,
                    activeHint = null,
                    validTargets = engine.getEmptyBoardCoords()
                )
            }
        }
    }

    fun onTrayDragStart(block: TrayBlock, rootOffset: Offset) {
        val current = _uiState.value
        if (current.isMerging || current.isVictory || current.isGameOver || block.isUsed) return
        soundSynthesizer.playSelect()
        _uiState.update {
            it.copy(
                draggingBlock = block,
                dragRootOffset = rootOffset,
                selectedTrayBlockId = block.id,
                selectedBoardCoord = null,
                activeHint = null,
                validTargets = engine.getEmptyBoardCoords(),
                hoverDropCoord = null
            )
        }
    }

    fun onTrayDragMove(rootOffset: Offset, candidateHex: HexCoord?) {
        val current = _uiState.value
        if (current.draggingBlock == null) return
        val validHover = candidateHex?.takeIf { hex ->
            hex in engine.validGridCoords && engine.getTileAt(hex) == null
        }
        _uiState.update {
            it.copy(
                dragRootOffset = rootOffset,
                hoverDropCoord = validHover
            )
        }
    }

    fun onTrayDragEnd(droppedInsideBoard: Boolean) {
        val current = _uiState.value
        val dragged = current.draggingBlock ?: return
        val targetHex = current.hoverDropCoord

        _uiState.update {
            it.copy(
                draggingBlock = null,
                dragRootOffset = null,
                hoverDropCoord = null
            )
        }

        if (targetHex != null) {
            executeTrayPlacement(dragged.id, targetHex)
        } else if (droppedInsideBoard) {
            triggerInvalidMove()
            _uiState.update {
                it.copy(
                    selectedTrayBlockId = null,
                    validTargets = emptyList()
                )
            }
        }
    }

    fun onBoardCellTapped(coord: HexCoord) {
        val current = _uiState.value
        if (current.isMerging || current.isVictory || current.isGameOver) return

        // Case 1: Tray block currently selected via tap
        val trayId = current.selectedTrayBlockId
        if (trayId != null) {
            if (engine.getTileAt(coord) != null) {
                selectBoardTile(coord)
            } else {
                executeTrayPlacement(trayId, coord)
            }
            return
        }

        // Case 2: Board tile currently selected for sliding
        val selectedFrom = current.selectedBoardCoord
        if (selectedFrom != null) {
            if (selectedFrom == coord) {
                soundSynthesizer.playClick()
                _uiState.update {
                    it.copy(selectedBoardCoord = null, validTargets = emptyList())
                }
                return
            }
            if (coord in current.validTargets) {
                executeBoardSlide(selectedFrom, coord)
                return
            }
            if (engine.getTileAt(coord) != null) {
                selectBoardTile(coord)
                return
            }
            triggerInvalidMove()
            _uiState.update {
                it.copy(selectedBoardCoord = null, validTargets = emptyList())
            }
            return
        }

        // Case 3: Nothing selected yet
        if (engine.getTileAt(coord) != null) {
            selectBoardTile(coord)
        } else {
            triggerInvalidMove()
        }
    }

    private fun selectBoardTile(coord: HexCoord) {
        soundSynthesizer.playSelect()
        val targets = engine.getValidSlideTargets(coord)
        _uiState.update {
            it.copy(
                selectedBoardCoord = coord,
                selectedTrayBlockId = null,
                activeHint = null,
                validTargets = targets
            )
        }
    }

    private fun executeTrayPlacement(blockId: String, destCoord: HexCoord) {
        val result = engine.placeTrayBlock(blockId, destCoord)
        handleMoveResult(result, destCoord)
    }

    private fun executeBoardSlide(fromCoord: HexCoord, destCoord: HexCoord) {
        val result = engine.moveTile(fromCoord, destCoord)
        handleMoveResult(result, destCoord)
    }

    private fun handleMoveResult(result: MoveResult, destCoord: HexCoord) {
        when (result) {
            MoveResult.InvalidMove -> {
                triggerInvalidMove()
            }
            is MoveResult.PlacedOrSlid -> {
                soundSynthesizer.playMove()
                if (result.trayReplenished) {
                    soundSynthesizer.playSuccess()
                }
                syncPostMoveState(merged = null, destCoord = destCoord)
            }
            is MoveResult.Merged -> {
                soundSynthesizer.playMerge(
                    tierNumber = result.initialTier.tierNumber,
                    combo = result.comboCount
                )
                if (result.trayReplenished) {
                    soundSynthesizer.playSuccess()
                }
                syncPostMoveState(merged = result, destCoord = destCoord)
            }
        }
    }

    private fun syncPostMoveState(merged: MoveResult.Merged?, destCoord: HexCoord) {
        val burstSparkles = if (merged != null) {
            buildSparkleBurst(destCoord, merged.mergedTile.tier.color)
        } else {
            emptyList()
        }

        _uiState.update {
            it.copy(
                tiles = engine.getTiles(),
                trayBlocks = engine.trayBlocks,
                selectedBoardCoord = null,
                selectedTrayBlockId = null,
                validTargets = emptyList(),
                activeHint = null,
                movesLeft = engine.movesLeft,
                score = engine.score,
                mergesCount = engine.mergesCount,
                highestTier = engine.highestTier,
                canUndo = engine.canUndo,
                isMerging = true,
                sparkles = burstSparkles
            )
        }

        viewModelScope.launch {
            delay(320L)
            engine.clearTransientFlags()
            val won = engine.isObjectiveMet()
            val lost = !won && engine.isGameOver()

            if (won) {
                val stars = engine.calculateStars()
                val levelId = _uiState.value.levelConfig.id
                val finalScore = engine.score
                val coinsBonus = 25 + stars * 10 + engine.movesLeft * 5
                soundSynthesizer.playVictory()
                progressRepository.recordLevelCompletion(
                    levelId = levelId,
                    starsEarned = stars,
                    scoreEarned = finalScore,
                    coinsBonus = coinsBonus
                )
                _uiState.update {
                    it.copy(
                        tiles = engine.getTiles(),
                        isMerging = false,
                        sparkles = emptyList(),
                        isVictory = true,
                        starsEarned = stars,
                        coinsEarned = coinsBonus
                    )
                }
            } else {
                if (lost) {
                    soundSynthesizer.playGameOver()
                }
                _uiState.update {
                    it.copy(
                        tiles = engine.getTiles(),
                        isMerging = false,
                        sparkles = emptyList(),
                        isGameOver = lost
                    )
                }
            }
        }
    }

    private fun buildSparkleBurst(
        coord: HexCoord,
        color: androidx.compose.ui.graphics.Color
    ): List<SparkleParticle> {
        return List(12) { index ->
            val baseAngle = (2.0 * PI * index / 12.0).toFloat()
            val jitter = ((Random.nextFloat() - 0.5f) * 0.35f)
            SparkleParticle(
                id = "sp-${System.nanoTime()}-$index",
                coord = coord,
                color = color,
                angleRad = baseAngle + jitter,
                distanceFactor = 0.85f + Random.nextFloat() * 0.7f,
                radiusDp = 2.5f + Random.nextFloat() * 2.5f
            )
        }
    }

    private fun triggerInvalidMove() {
        soundSynthesizer.playInvalidMove()
        _uiState.update { it.copy(shakeTrigger = it.shakeTrigger + 1) }
    }

    fun undoMove() {
        val current = _uiState.value
        if (current.isMerging || !engine.canUndo) return
        if (engine.undo()) {
            soundSynthesizer.playUndo()
            _uiState.update {
                it.copy(
                    tiles = engine.getTiles(),
                    trayBlocks = engine.trayBlocks,
                    movesLeft = engine.movesLeft,
                    score = engine.score,
                    mergesCount = engine.mergesCount,
                    highestTier = engine.highestTier,
                    canUndo = engine.canUndo,
                    selectedBoardCoord = null,
                    selectedTrayBlockId = null,
                    validTargets = emptyList(),
                    activeHint = null,
                    isGameOver = false
                )
            }
        }
    }

    fun requestHint(currentHintsRemaining: Int) {
        val current = _uiState.value
        if (current.isMerging || current.isVictory || current.isGameOver) return

        if (currentHintsRemaining > 0) {
            viewModelScope.launch {
                progressRepository.updateProgress {
                    it.copy(hintsRemaining = (it.hintsRemaining - 1).coerceAtLeast(0))
                }
                applySolverHint()
            }
        } else {
            soundSynthesizer.playClick()
            _uiState.update {
                it.copy(
                    breakModalState = BreakModalState(
                        isOpen = true,
                        type = BreakType.REWARDED,
                        rewardKind = RewardKind.HINT
                    )
                )
            }
        }
    }

    private fun applySolverHint() {
        val suggestion = HintSolver.findBestMoveHint(
            validCoords = engine.validGridCoords,
            currentTiles = engine.getTiles(),
            trayBlocks = engine.trayBlocks
        ) ?: return

        soundSynthesizer.playHint()
        when (suggestion.sourceType) {
            HintSourceType.TRAY -> {
                _uiState.update {
                    it.copy(
                        activeHint = suggestion,
                        selectedTrayBlockId = suggestion.trayBlockId,
                        selectedBoardCoord = null,
                        validTargets = listOf(suggestion.to)
                    )
                }
            }
            HintSourceType.BOARD -> {
                _uiState.update {
                    it.copy(
                        activeHint = suggestion,
                        selectedBoardCoord = suggestion.from,
                        selectedTrayBlockId = null,
                        validTargets = listOf(suggestion.to)
                    )
                }
            }
        }
    }

    fun requestExtraMovesBreak() {
        soundSynthesizer.playClick()
        _uiState.update {
            it.copy(
                breakModalState = BreakModalState(
                    isOpen = true,
                    type = BreakType.REWARDED,
                    rewardKind = RewardKind.EXTRA_MOVES
                )
            )
        }
    }

    fun completeBreakReward() {
        val modal = _uiState.value.breakModalState
        soundSynthesizer.playSuccess()
        _uiState.update { it.copy(breakModalState = BreakModalState(isOpen = false)) }

        when (modal.rewardKind) {
            RewardKind.EXTRA_MOVES -> {
                engine.addExtraMoves(3)
                _uiState.update {
                    it.copy(
                        movesLeft = engine.movesLeft,
                        isGameOver = false
                    )
                }
            }
            RewardKind.HINT -> {
                applySolverHint()
            }
            null -> {
                pendingBreakDismissAction?.invoke()
                pendingBreakDismissAction = null
            }
        }
    }

    fun dismissBreakModal() {
        soundSynthesizer.playClick()
        _uiState.update { it.copy(breakModalState = BreakModalState(isOpen = false)) }
        pendingBreakDismissAction?.invoke()
        pendingBreakDismissAction = null
    }

    fun onNextLevelClicked(
        onAdvanceToLevel: (Int) -> Unit,
        onExitToMap: () -> Unit
    ) {
        val currentId = _uiState.value.levelConfig.id
        _uiState.update { it.copy(isVictory = false) }
        val nextId = currentId + 1
        if (nextId > 100) {
            onExitToMap()
            return
        }

        levelsCompletedSinceBreak++
        if (levelsCompletedSinceBreak >= 3 && currentId > 2) {
            levelsCompletedSinceBreak = 0
            pendingBreakDismissAction = { onAdvanceToLevel(nextId) }
            _uiState.update {
                it.copy(
                    breakModalState = BreakModalState(
                        isOpen = true,
                        type = BreakType.INTERSTITIAL,
                        rewardKind = null
                    )
                )
            }
        } else {
            onAdvanceToLevel(nextId)
        }
    }

    class Factory(
        private val progressRepository: ProgressRepository,
        private val soundSynthesizer: SoundSynthesizer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(progressRepository, soundSynthesizer) as T
        }
    }
}
