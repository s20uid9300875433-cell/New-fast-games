package com.hexacolor.quest.logic

import com.hexacolor.quest.model.BoardTile
import com.hexacolor.quest.model.HexCoord
import com.hexacolor.quest.model.LevelConfig
import com.hexacolor.quest.model.MoveSnapshot
import com.hexacolor.quest.model.ObjectiveType
import com.hexacolor.quest.model.TileTier
import com.hexacolor.quest.model.TrayBlock
import java.util.UUID
import kotlin.random.Random

sealed class MoveResult {
    data class PlacedOrSlid(
        val tile: BoardTile,
        val trayReplenished: Boolean
    ) : MoveResult()

    data class Merged(
        val mergedTile: BoardTile,
        val initialTier: TileTier,
        val pointsAwarded: Int,
        val comboCount: Int,
        val totalMergedTiles: Int,
        val trayReplenished: Boolean
    ) : MoveResult()

    data object InvalidMove : MoveResult()
}

class GameEngine(
    val levelConfig: LevelConfig,
    private val random: Random = Random.Default
) {
    private val tiles = linkedMapOf<HexCoord, BoardTile>()
    private val undoStack = mutableListOf<MoveSnapshot>()

    var movesLeft: Int = levelConfig.moveLimit
        private set
    var score: Int = 0
        private set
    var mergesCount: Int = 0
        private set
    var highestTier: TileTier = TileTier.TIER_1
        private set
    var trayBlocks: List<TrayBlock> = emptyList()
        private set

    private var dealIndex: Int = 0

    val validGridCoords: List<HexCoord> = HexMath.generateGrid(
        radius = levelConfig.radius,
        blocked = levelConfig.blockedCoords
    )
    private val validGridSet: Set<HexCoord> = validGridCoords.toSet()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    init {
        reset()
    }

    fun reset() {
        tiles.clear()
        undoStack.clear()
        movesLeft = levelConfig.moveLimit
        score = 0
        mergesCount = 0
        dealIndex = 0

        var maxTier = TileTier.TIER_1
        levelConfig.initialTiles.forEachIndexed { idx, (coord, tier) ->
            if (coord in validGridSet) {
                tiles[coord] = BoardTile(
                    id = "init-${levelConfig.id}-$idx-${UUID.randomUUID()}",
                    coord = coord,
                    tier = tier
                )
                if (tier.tierNumber > maxTier.tierNumber) {
                    maxTier = tier
                }
            }
        }
        highestTier = maxTier
        dealTrayBlocks()
    }

    fun dealTrayBlocks() {
        val maxPossible = when (levelConfig.difficulty) {
            "Normal", "Tutorial" -> 3
            "Hard" -> 4
            else -> 5 // Hard+, Very Hard, Expert, Master (Tier 6 synthesized via merging)
        }

        val existingTiers = tiles.values.map { it.tier.tierNumber }.distinct()
        val newBlocks = mutableListOf<TrayBlock>()
        dealIndex++

        for (i in 0 until 3) {
            val tierNum = if (existingTiers.isNotEmpty() && random.nextFloat() < 0.75f) {
                val picked = existingTiers[random.nextInt(existingTiers.size)]
                minOf(picked, maxPossible)
            } else {
                random.nextInt(1, maxPossible + 1)
            }
            val tier = TileTier.fromNumber(tierNum)
            newBlocks.add(
                TrayBlock(
                    id = "tray-${levelConfig.id}-$dealIndex-$i-${UUID.randomUUID()}",
                    tier = tier,
                    isUsed = false
                )
            )
        }
        trayBlocks = newBlocks
    }

    fun getTiles(): List<BoardTile> = tiles.values.toList()

    fun getTileAt(coord: HexCoord): BoardTile? = tiles[coord]

    fun clearTransientFlags() {
        val keys = tiles.keys.toList()
        for (k in keys) {
            val t = tiles[k] ?: continue
            if (t.isMerging || t.isNew) {
                tiles[k] = t.copy(isMerging = false, isNew = false)
            }
        }
    }

    fun getValidSlideTargets(coord: HexCoord): List<HexCoord> {
        if (coord !in tiles) return emptyList()
        return HexMath.getNeighbors(coord).filter { nb ->
            nb in validGridSet && nb !in tiles
        }
    }

    fun getEmptyBoardCoords(): List<HexCoord> {
        return validGridCoords.filter { it !in tiles }
    }

    fun placeTrayBlock(blockId: String, to: HexCoord): MoveResult {
        if (movesLeft <= 0) return MoveResult.InvalidMove
        val block = trayBlocks.find { it.id == blockId && !it.isUsed } ?: return MoveResult.InvalidMove
        if (to !in validGridSet || to in tiles) return MoveResult.InvalidMove

        pushSnapshot()

        movesLeft--
        val placedTile = BoardTile(
            id = "placed-${UUID.randomUUID()}",
            coord = to,
            tier = block.tier,
            isNew = true
        )
        tiles[to] = placedTile
        if (block.tier.tierNumber > highestTier.tierNumber) {
            highestTier = block.tier
        }

        trayBlocks = trayBlocks.map { if (it.id == blockId) it.copy(isUsed = true) else it }
        val replenished = if (trayBlocks.all { it.isUsed }) {
            dealTrayBlocks()
            true
        } else {
            false
        }

        return evaluateMerges(to, placedTile, replenished)
    }

    fun moveTile(from: HexCoord, to: HexCoord): MoveResult {
        if (movesLeft <= 0) return MoveResult.InvalidMove
        val movingTile = tiles[from] ?: return MoveResult.InvalidMove
        val validTargets = getValidSlideTargets(from)
        if (to !in validTargets) return MoveResult.InvalidMove

        pushSnapshot()

        movesLeft--
        tiles.remove(from)
        val movedTile = movingTile.copy(coord = to, isMerging = false, isNew = false)
        tiles[to] = movedTile

        return evaluateMerges(to, movedTile, trayReplenished = false)
    }

    private fun pushSnapshot() {
        undoStack.add(
            MoveSnapshot(
                tiles = tiles.values.map { it.copy(isMerging = false, isNew = false) },
                trayBlocks = trayBlocks.toList(),
                movesLeft = movesLeft,
                score = score,
                mergesCount = mergesCount,
                highestTier = highestTier
            )
        )
    }

    private fun evaluateMerges(
        at: HexCoord,
        placedTile: BoardTile,
        trayReplenished: Boolean
    ): MoveResult {
        val group = HexMath.findConnectedGroup(at, tiles)
        if (group.size < 3) {
            return MoveResult.PlacedOrSlid(
                tile = placedTile,
                trayReplenished = trayReplenished
            )
        }

        val initialTier = placedTile.tier
        var currentGroup = group
        var combo = 0
        var totalPoints = 0
        var totalMergedCount = 0
        var finalUpgraded = placedTile

        while (currentGroup.size >= 3) {
            combo++
            val groupTier = currentGroup.first().tier
            val nextTier = groupTier.nextTier()
            totalMergedCount += currentGroup.size

            currentGroup.forEach { t -> tiles.remove(t.coord) }

            finalUpgraded = BoardTile(
                id = "merged-${UUID.randomUUID()}",
                coord = at,
                tier = nextTier,
                isMerging = true
            )
            tiles[at] = finalUpgraded

            if (nextTier.tierNumber > highestTier.tierNumber) {
                highestTier = nextTier
            }

            val gained = currentGroup.size * groupTier.scoreValue * combo
            score += gained
            totalPoints += gained
            mergesCount++

            // If already at max tier (Tier 6), do not infinite-loop merging Tier 6 into Tier 6
            if (groupTier == TileTier.TIER_6) {
                break
            }
            currentGroup = HexMath.findConnectedGroup(at, tiles)
        }

        return MoveResult.Merged(
            mergedTile = finalUpgraded,
            initialTier = initialTier,
            pointsAwarded = totalPoints,
            comboCount = combo,
            totalMergedTiles = totalMergedCount,
            trayReplenished = trayReplenished
        )
    }

    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        val last = undoStack.removeAt(undoStack.lastIndex)
        tiles.clear()
        last.tiles.forEach { t -> tiles[t.coord] = t }
        if (last.trayBlocks.isNotEmpty()) {
            trayBlocks = last.trayBlocks
        }
        movesLeft = last.movesLeft
        score = last.score
        mergesCount = last.mergesCount
        highestTier = last.highestTier
        return true
    }

    fun addExtraMoves(extraMoves: Int) {
        if (extraMoves > 0) {
            movesLeft += extraMoves
        }
    }

    fun isObjectiveMet(): Boolean {
        val obj = levelConfig.objective
        return when (obj.type) {
            ObjectiveType.REACH_TIER -> highestTier.tierNumber >= (obj.targetTier?.tierNumber ?: 3)
            ObjectiveType.MERGE_COUNT -> mergesCount >= (obj.count ?: 1)
            ObjectiveType.TARGET_SCORE -> score >= (obj.targetScore ?: 500)
        }
    }

    fun isGameOver(): Boolean {
        if (isObjectiveMet()) return false
        if (movesLeft <= 0) return true
        // Also check if board is completely full and no slides exist
        val hasEmptyCells = getEmptyBoardCoords().isNotEmpty()
        return !hasEmptyCells
    }

    fun calculateStars(): Int {
        val th = levelConfig.starThresholds
        return when {
            movesLeft >= th.threeStars -> 3
            movesLeft >= th.twoStars -> 2
            else -> 1
        }
    }
}
