package com.hexacolor.quest.data

import com.hexacolor.quest.model.ChapterInfo
import com.hexacolor.quest.model.HexCoord
import com.hexacolor.quest.model.LevelConfig
import com.hexacolor.quest.model.LevelObjective
import com.hexacolor.quest.model.ObjectiveType
import com.hexacolor.quest.model.StarThresholds
import com.hexacolor.quest.model.TileTier
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object LevelRepository {

    val CHAPTERS: List<ChapterInfo> = listOf(
        ChapterInfo(1, "Spark", "1-20", "Normal & Hard", 1, 20),
        ChapterInfo(2, "Cascade", "21-40", "Hard+ & Very Hard", 21, 40),
        ChapterInfo(3, "Prism", "41-60", "Very Hard & Expert", 41, 60),
        ChapterInfo(4, "Alchemy", "61-80", "Expert & Master", 61, 80),
        ChapterInfo(5, "Apex", "81-100", "Master Strategy", 81, 100)
    )

    private val allLevels: List<LevelConfig> by lazy {
        generateAllLevels()
    }

    fun getAllLevels(): List<LevelConfig> = allLevels

    fun getLevel(levelId: Int): LevelConfig {
        val clamped = levelId.coerceIn(1, 100)
        return allLevels[clamped - 1]
    }

    fun getLevelsForChapter(chapterId: Int): List<ChapterInfoWithLevels> {
        val ch = CHAPTERS.firstOrNull { it.id == chapterId } ?: CHAPTERS.first()
        return listOf(
            ChapterInfoWithLevels(
                chapter = ch,
                levels = allLevels.subList(ch.startLevel - 1, ch.endLevel)
            )
        )
    }

    data class ChapterInfoWithLevels(
        val chapter: ChapterInfo,
        val levels: List<LevelConfig>
    )

    private class DeterministicPrng(seed: Long) {
        private var s: Long = seed
        fun nextDouble(): Double {
            s = (s * 9301L + 49297L) % 233280L
            return s.toDouble() / 233280.0
        }
    }

    private fun handcraftedLevels(): Map<Int, LevelConfig> = mapOf(
        1 to LevelConfig(
            id = 1,
            chapter = 1,
            name = "Ocean Pearl",
            difficulty = "Normal",
            radius = 2,
            moveLimit = 12,
            objective = LevelObjective(
                type = ObjectiveType.REACH_TIER,
                targetTier = TileTier.TIER_3,
                description = "Synthesize an Emerald Clover (Tier 3)"
            ),
            initialTiles = listOf(
                HexCoord(-1, 0) to TileTier.TIER_1,
                HexCoord(0, -1) to TileTier.TIER_1,
                HexCoord(1, 0) to TileTier.TIER_2,
                HexCoord(0, 1) to TileTier.TIER_2
            ),
            starThresholds = StarThresholds(oneStar = 0, twoStars = 3, threeStars = 6),
            tutorialTip = "Place blocks from the bottom tray or slide existing tiles to connect 3 identical blocks. Merge Ruby Hearts into Amber Stars, then merge Amber Stars into the Emerald Clover!"
        ),
        2 to LevelConfig(
            id = 2,
            chapter = 1,
            name = "Sunlit Cavern",
            difficulty = "Normal",
            radius = 2,
            moveLimit = 12,
            objective = LevelObjective(
                type = ObjectiveType.MERGE_COUNT,
                count = 3,
                description = "Perform 3 color merges on the board"
            ),
            initialTiles = listOf(
                HexCoord(-1, 0) to TileTier.TIER_1,
                HexCoord(-1, 1) to TileTier.TIER_1,
                HexCoord(1, -1) to TileTier.TIER_2,
                HexCoord(1, 0) to TileTier.TIER_2,
                HexCoord(0, 1) to TileTier.TIER_1
            ),
            starThresholds = StarThresholds(oneStar = 0, twoStars = 3, threeStars = 6),
            tutorialTip = "Look ahead! Plan your placements so you set up multiple 3-block merges in sequence."
        ),
        3 to LevelConfig(
            id = 3,
            chapter = 1,
            name = "Clover Cove",
            difficulty = "Normal",
            radius = 2,
            moveLimit = 14,
            objective = LevelObjective(
                type = ObjectiveType.REACH_TIER,
                targetTier = TileTier.TIER_4,
                description = "Synthesize an Azure Hexa (Tier 4)"
            ),
            initialTiles = listOf(
                HexCoord(-1, -1) to TileTier.TIER_1,
                HexCoord(-1, 0) to TileTier.TIER_1,
                HexCoord(0, 1) to TileTier.TIER_2,
                HexCoord(1, 0) to TileTier.TIER_2,
                HexCoord(0, -1) to TileTier.TIER_3,
                HexCoord(1, -1) to TileTier.TIER_3
            ),
            starThresholds = StarThresholds(oneStar = 0, twoStars = 4, threeStars = 7),
            tutorialTip = "Work your way up the tiers: combine Hearts into Stars, Stars into Clovers, and Clovers into Azure Hexas!"
        ),
        4 to LevelConfig(
            id = 4,
            chapter = 1,
            name = "Deep Blue Tide",
            difficulty = "Normal",
            radius = 2,
            moveLimit = 14,
            objective = LevelObjective(
                type = ObjectiveType.TARGET_SCORE,
                targetScore = 650,
                description = "Score 650 points from color merges"
            ),
            initialTiles = listOf(
                HexCoord(-2, 0) to TileTier.TIER_1,
                HexCoord(-1, 0) to TileTier.TIER_1,
                HexCoord(0, 1) to TileTier.TIER_2,
                HexCoord(1, 0) to TileTier.TIER_2,
                HexCoord(0, -1) to TileTier.TIER_3
            ),
            starThresholds = StarThresholds(oneStar = 0, twoStars = 4, threeStars = 7),
            tutorialTip = "Merging higher tiers yields exponential point rewards! Chain combos multiply your points even further."
        ),
        5 to LevelConfig(
            id = 5,
            chapter = 1,
            name = "The Coral Nexus",
            difficulty = "Normal",
            radius = 2,
            moveLimit = 13,
            objective = LevelObjective(
                type = ObjectiveType.REACH_TIER,
                targetTier = TileTier.TIER_4,
                description = "Synthesize an Azure Hexa (Tier 4)"
            ),
            initialTiles = listOf(
                HexCoord(-1, 0) to TileTier.TIER_2,
                HexCoord(1, -1) to TileTier.TIER_2,
                HexCoord(0, 1) to TileTier.TIER_1,
                HexCoord(0, 2) to TileTier.TIER_1,
                HexCoord(-1, 2) to TileTier.TIER_3,
                HexCoord(1, 0) to TileTier.TIER_3
            ),
            starThresholds = StarThresholds(oneStar = 0, twoStars = 3, threeStars = 6),
            tutorialTip = "The central hub connects to 6 neighboring cells. Use it as the convergence point for your highest tier merges!"
        )
    )

    private fun generateAllLevels(): List<LevelConfig> {
        val handcrafted = handcraftedLevels()
        val prefixes = listOf(
            "Spark", "Prism", "Orbit", "Cascade", "Nexus",
            "Chroma", "Radiance", "Vortex", "Crown", "Zenith"
        )
        val suffixes = listOf(
            "Valley", "Sanctum", "Trail", "Matrix", "Loom",
            "Spire", "Gateway", "Cavern", "Crest", "Peak"
        )

        val levels = mutableListOf<LevelConfig>()

        for (id in 1..100) {
            handcrafted[id]?.let {
                levels.add(it)
                continue
            }

            val chapter = ((id - 1) / 20) + 1
            val rng = DeterministicPrng(id * 7919L + 104729L)

            val difficulty = when {
                id <= 5 -> "Normal"
                id <= 15 -> "Hard"
                id <= 30 -> "Hard+"
                id <= 50 -> "Very Hard"
                id <= 75 -> "Expert"
                else -> "Master"
            }

            val radius = if (id <= 15) 2 else 3

            val blockedCoords = mutableListOf<HexCoord>()
            if (id in 31..50) {
                if (id % 2 == 0) {
                    blockedCoords.add(HexCoord(0, 0))
                }
                if (id % 3 == 0) {
                    blockedCoords.add(HexCoord(1, -1))
                    blockedCoords.add(HexCoord(-1, 1))
                }
            } else if (id in 51..75) {
                if (id % 2 == 1) {
                    blockedCoords.add(HexCoord(0, 0))
                }
                if (id % 4 == 0) {
                    blockedCoords.add(HexCoord(1, 0))
                    blockedCoords.add(HexCoord(-1, 0))
                }
            } else if (id >= 76) {
                if (id % 3 == 0) {
                    blockedCoords.add(HexCoord(0, 0))
                    blockedCoords.add(HexCoord(1, -1))
                    blockedCoords.add(HexCoord(-1, 1))
                } else if (id % 2 == 0) {
                    blockedCoords.add(HexCoord(0, 1))
                    blockedCoords.add(HexCoord(0, -1))
                }
            }

            var targetTierNum = 3
            var objType = ObjectiveType.REACH_TIER
            var targetCount = 1
            var targetScore = 500
            var moveLimit = 10

            when {
                id <= 15 -> {
                    targetTierNum = if (id <= 10) 3 else 4
                    if (id % 3 == 0) objType = ObjectiveType.MERGE_COUNT
                    targetCount = 3
                    targetScore = 450 + (id - 5) * 60
                    moveLimit = max(8, 11 - (id - 6) / 3)
                }
                id <= 30 -> {
                    targetTierNum = if (id <= 22) 4 else 5
                    if (id % 3 == 0) objType = ObjectiveType.MERGE_COUNT
                    else if (id % 4 == 0) objType = ObjectiveType.TARGET_SCORE
                    targetCount = 4
                    targetScore = 900 + (id - 15) * 80
                    moveLimit = max(10, 14 - (id - 15) / 4)
                }
                id <= 50 -> {
                    targetTierNum = if (id % 2 == 0) 4 else 5
                    if (id % 3 == 0) objType = ObjectiveType.MERGE_COUNT
                    else if (id % 4 == 0) objType = ObjectiveType.TARGET_SCORE
                    targetCount = 4 + (id - 30) / 7
                    targetScore = 1500 + (id - 30) * 110
                    moveLimit = max(11, 15 - (id - 30) / 5)
                }
                id <= 75 -> {
                    targetTierNum = if (id > 65) 5 else 4
                    if (id % 3 == 0) objType = ObjectiveType.MERGE_COUNT
                    else if (id % 4 == 0) objType = ObjectiveType.TARGET_SCORE
                    targetCount = 5 + (id - 50) / 8
                    targetScore = 2600 + (id - 50) * 130
                    moveLimit = max(12, 16 - (id - 50) / 6)
                }
                else -> {
                    targetTierNum = if (id > 88) 6 else 5
                    if (id % 3 == 0) objType = ObjectiveType.MERGE_COUNT
                    else if (id % 5 == 0) objType = ObjectiveType.TARGET_SCORE
                    targetCount = 6
                    targetScore = 4500 + (id - 75) * 180
                    moveLimit = max(13, 17 - (id - 75) / 8)
                }
            }

            val targetTier = TileTier.fromNumber(targetTierNum)
            val description = when (objType) {
                ObjectiveType.REACH_TIER -> "Synthesize a ${targetTier.tierName} (Tier $targetTierNum)"
                ObjectiveType.MERGE_COUNT -> "Perform $targetCount color merges"
                ObjectiveType.TARGET_SCORE -> "Score $targetScore points"
            }

            val blockedSet = blockedCoords.toSet()
            val availableCoords = mutableListOf<HexCoord>()
            for (q in -radius..radius) {
                val r1 = max(-radius, -q - radius)
                val r2 = min(radius, -q + radius)
                for (r in r1..r2) {
                    val c = HexCoord(q, r)
                    if (c !in blockedSet) {
                        availableCoords.add(c)
                    }
                }
            }

            // Deterministic Fisher-Yates shuffle matching JS implementation
            for (i in availableCoords.lastIndex downTo 1) {
                val j = floor(rng.nextDouble() * (i + 1)).toInt()
                val tmp = availableCoords[i]
                availableCoords[i] = availableCoords[j]
                availableCoords[j] = tmp
            }

            val numTiles = min(
                if (radius == 2) 8 else 14,
                5 + (id / 10) + floor(rng.nextDouble() * 3.0).toInt()
            )

            val primaryTier = TileTier.fromNumber(max(1, targetTierNum - 2))
            val secondaryTier = TileTier.fromNumber(max(1, targetTierNum - 1))

            val initialTiles = mutableListOf<Pair<HexCoord, TileTier>>()
            for (i in 0 until min(numTiles, availableCoords.size)) {
                val coord = availableCoords[i]
                val tier = when {
                    i < 3 -> primaryTier
                    i < 6 -> secondaryTier
                    i < 8 -> TileTier.TIER_1
                    else -> {
                        val randTier = 1 + floor(rng.nextDouble() * min(4, targetTierNum)).toInt()
                        TileTier.fromNumber(randTier)
                    }
                }
                initialTiles.add(coord to tier)
            }

            val pIdx = (id * 3) % prefixes.size
            val sIdx = (id * 7) % suffixes.size
            val levelName = "${prefixes[pIdx]} ${suffixes[sIdx]}"

            levels.add(
                LevelConfig(
                    id = id,
                    chapter = chapter,
                    name = levelName,
                    difficulty = difficulty,
                    radius = radius,
                    blockedCoords = blockedCoords,
                    initialTiles = initialTiles,
                    moveLimit = moveLimit,
                    objective = LevelObjective(
                        type = objType,
                        targetTier = targetTier,
                        count = targetCount,
                        targetScore = targetScore,
                        description = description
                    ),
                    starThresholds = StarThresholds(
                        oneStar = 0,
                        twoStars = max(2, moveLimit / 4),
                        threeStars = max(4, moveLimit / 2)
                    ),
                    tutorialTip = null
                )
            )
        }

        return levels
    }
}
