package com.hexacolor.quest.logic

import com.hexacolor.quest.model.BoardTile
import com.hexacolor.quest.model.HexCoord
import com.hexacolor.quest.model.HintSourceType
import com.hexacolor.quest.model.HintSuggestion
import com.hexacolor.quest.model.TrayBlock

object HintSolver {
    /**
     * Finds the optimal move (either placing a block from the bottom tray OR sliding an existing tile)
     * that triggers an immediate 3+ tile merge, or prepares a merge adjacent to matching tiles.
     */
    fun findBestMoveHint(
        validCoords: List<HexCoord>,
        currentTiles: List<BoardTile>,
        trayBlocks: List<TrayBlock> = emptyList()
    ): HintSuggestion? {
        val validSet = validCoords.toSet()
        val tilesMap = currentTiles.associateBy { it.coord }
        val emptyCoords = validCoords.filter { it !in tilesMap }

        var bestSuggestion: HintSuggestion? = null
        var bestScore = -1

        val availableTrayBlocks = trayBlocks.filter { !it.isUsed }

        // 1. Check Bottom Tray Blocks for immediate 3+ merges
        for (block in availableTrayBlocks) {
            for (dest in emptyCoords) {
                val simMap = tilesMap.toMutableMap()
                simMap[dest] = BoardTile(
                    id = "sim",
                    coord = dest,
                    tier = block.tier
                )

                val group = HexMath.findConnectedGroup(dest, simMap)
                if (group.size >= 3) {
                    val moveScore = group.size * 120 + block.tier.tierNumber * 60
                    if (moveScore > bestScore) {
                        bestScore = moveScore
                        val nextTier = block.tier.nextTier()
                        bestSuggestion = HintSuggestion(
                            sourceType = HintSourceType.TRAY,
                            trayBlockId = block.id,
                            to = dest,
                            tier = block.tier,
                            groupSize = group.size,
                            resultingTier = nextTier,
                            explanation = "Place T${block.tier.tierNumber} block from tray to merge ${group.size} tiles into T${nextTier.tierNumber}!"
                        )
                    }
                }
            }
        }

        // 2. Check Board Tile Slides for immediate 3+ merges
        for (tile in currentTiles) {
            val originCoord = tile.coord
            for (dest in HexMath.getNeighbors(originCoord)) {
                if (dest !in validSet || dest in tilesMap) continue

                val simMap = tilesMap.toMutableMap()
                simMap.remove(originCoord)
                simMap[dest] = tile.copy(coord = dest)

                val group = HexMath.findConnectedGroup(dest, simMap)
                if (group.size >= 3) {
                    val moveScore = group.size * 100 + tile.tier.tierNumber * 50
                    if (moveScore > bestScore) {
                        bestScore = moveScore
                        val nextTier = tile.tier.nextTier()
                        bestSuggestion = HintSuggestion(
                            sourceType = HintSourceType.BOARD,
                            from = originCoord,
                            to = dest,
                            tier = tile.tier,
                            groupSize = group.size,
                            resultingTier = nextTier,
                            explanation = "Slide T${tile.tier.tierNumber} tile to merge ${group.size} tiles into T${nextTier.tierNumber}!"
                        )
                    }
                }
            }
        }

        // 3. Fallback: Tray placement adjacent to matching colors
        if (bestSuggestion == null && availableTrayBlocks.isNotEmpty()) {
            for (block in availableTrayBlocks) {
                for (dest in emptyCoords) {
                    val matchingNeighbors = HexMath.getNeighbors(dest).count { nb ->
                        tilesMap[nb]?.tier == block.tier
                    }
                    if (matchingNeighbors > 0) {
                        val score = matchingNeighbors * 25 + block.tier.tierNumber
                        if (score > bestScore) {
                            bestScore = score
                            bestSuggestion = HintSuggestion(
                                sourceType = HintSourceType.TRAY,
                                trayBlockId = block.id,
                                to = dest,
                                tier = block.tier,
                                groupSize = matchingNeighbors + 1,
                                resultingTier = block.tier,
                                explanation = "Place T${block.tier.tierNumber} block next to matching tiles to set up a merge."
                            )
                        }
                    }
                }
            }
        }

        // 4. Final fallback if board has no matching neighbors yet: suggest any valid slide or placement
        if (bestSuggestion == null) {
            val firstTray = availableTrayBlocks.firstOrNull()
            val firstEmpty = emptyCoords.firstOrNull()
            if (firstTray != null && firstEmpty != null) {
                bestSuggestion = HintSuggestion(
                    sourceType = HintSourceType.TRAY,
                    trayBlockId = firstTray.id,
                    to = firstEmpty,
                    tier = firstTray.tier,
                    groupSize = 1,
                    resultingTier = firstTray.tier,
                    explanation = "Place T${firstTray.tier.tierNumber} block on an open cell."
                )
            }
        }

        return bestSuggestion
    }
}
