package com.hexacolor.quest.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class HexCoord(
    val q: Int,
    val r: Int
) {
    val s: Int get() = -q - r

    override fun toString(): String = "$q,$r"

    companion object {
        fun fromString(key: String): HexCoord {
            val parts = key.split(",")
            return HexCoord(parts[0].toInt(), parts[1].toInt())
        }
    }
}

enum class SymbolType {
    HEART,
    STAR5,
    CLOVER,
    STAR6,
    MOON,
    CROWN
}

enum class TileTier(
    val tierNumber: Int,
    val tierName: String,
    val shortName: String,
    val color: Color,
    val lightColor: Color,
    val shadowColor: Color,
    val ridgeColor: Color,
    val ridgeHighlight: Color,
    val symbolType: SymbolType,
    val glyph: String,
    val scoreValue: Int
) {
    TIER_1(
        tierNumber = 1,
        tierName = "Ruby Heart",
        shortName = "Heart",
        color = Color(0xFFEF4444),
        lightColor = Color(0xFFFCA5A5),
        shadowColor = Color(0xFFB91C1C),
        ridgeColor = Color(0xFF7F1D1D),
        ridgeHighlight = Color(0xFFFECDD3),
        symbolType = SymbolType.HEART,
        glyph = "♥",
        scoreValue = 10
    ),
    TIER_2(
        tierNumber = 2,
        tierName = "Amber Star",
        shortName = "Star",
        color = Color(0xFFF59E0B),
        lightColor = Color(0xFFFDE68A),
        shadowColor = Color(0xFFB45309),
        ridgeColor = Color(0xFF78350F),
        ridgeHighlight = Color(0xFFFEF3C7),
        symbolType = SymbolType.STAR5,
        glyph = "★",
        scoreValue = 30
    ),
    TIER_3(
        tierNumber = 3,
        tierName = "Emerald Clover",
        shortName = "Clover",
        color = Color(0xFF10B981),
        lightColor = Color(0xFFA7F3D0),
        shadowColor = Color(0xFF047857),
        ridgeColor = Color(0xFF064E3B),
        ridgeHighlight = Color(0xFFD1FAE5),
        symbolType = SymbolType.CLOVER,
        glyph = "♣",
        scoreValue = 90
    ),
    TIER_4(
        tierNumber = 4,
        tierName = "Azure Hexa",
        shortName = "Hexa",
        color = Color(0xFF2563EB),
        lightColor = Color(0xFF93C5FD),
        shadowColor = Color(0xFF1D4ED8),
        ridgeColor = Color(0xFF172554),
        ridgeHighlight = Color(0xFFDBEAFE),
        symbolType = SymbolType.STAR6,
        glyph = "✶",
        scoreValue = 270
    ),
    TIER_5(
        tierNumber = 5,
        tierName = "Violet Moon",
        shortName = "Moon",
        color = Color(0xFF9333EA),
        lightColor = Color(0xFFE9D5FF),
        shadowColor = Color(0xFF6B21A8),
        ridgeColor = Color(0xFF3B0764),
        ridgeHighlight = Color(0xFFF3E8FF),
        symbolType = SymbolType.MOON,
        glyph = "☽",
        scoreValue = 810
    ),
    TIER_6(
        tierNumber = 6,
        tierName = "Prismatic Crown",
        shortName = "Crown",
        color = Color(0xFFEC4899),
        lightColor = Color(0xFFFDE047),
        shadowColor = Color(0xFFBE185D),
        ridgeColor = Color(0xFF700B32),
        ridgeHighlight = Color(0xFFFCE7F3),
        symbolType = SymbolType.CROWN,
        glyph = "👑",
        scoreValue = 2500
    );

    fun nextTier(): TileTier = when (this) {
        TIER_1 -> TIER_2
        TIER_2 -> TIER_3
        TIER_3 -> TIER_4
        TIER_4 -> TIER_5
        TIER_5 -> TIER_6
        TIER_6 -> TIER_6
    }

    companion object {
        fun fromNumber(number: Int): TileTier {
            return entries.firstOrNull { it.tierNumber == number.coerceIn(1, 6) } ?: TIER_1
        }
    }
}

data class BoardTile(
    val id: String,
    val coord: HexCoord,
    val tier: TileTier,
    val isMerging: Boolean = false,
    val isNew: Boolean = false
)

data class TrayBlock(
    val id: String,
    val tier: TileTier,
    val isUsed: Boolean = false
)

enum class ObjectiveType {
    REACH_TIER,
    MERGE_COUNT,
    TARGET_SCORE
}

data class LevelObjective(
    val type: ObjectiveType,
    val targetTier: TileTier? = null,
    val count: Int? = null,
    val targetScore: Int? = null,
    val description: String
)

data class StarThresholds(
    val oneStar: Int,
    val twoStars: Int,
    val threeStars: Int
)

data class LevelConfig(
    val id: Int,
    val chapter: Int,
    val name: String,
    val difficulty: String,
    val radius: Int,
    val blockedCoords: List<HexCoord> = emptyList(),
    val initialTiles: List<Pair<HexCoord, TileTier>>,
    val moveLimit: Int,
    val objective: LevelObjective,
    val starThresholds: StarThresholds,
    val tutorialTip: String? = null
)

data class ChapterInfo(
    val id: Int,
    val name: String,
    val range: String,
    val tierInfo: String,
    val startLevel: Int,
    val endLevel: Int
)

data class MoveSnapshot(
    val tiles: List<BoardTile>,
    val trayBlocks: List<TrayBlock>,
    val movesLeft: Int,
    val score: Int,
    val mergesCount: Int,
    val highestTier: TileTier
)

enum class HintSourceType {
    TRAY,
    BOARD
}

data class HintSuggestion(
    val sourceType: HintSourceType,
    val trayBlockId: String? = null,
    val from: HexCoord? = null,
    val to: HexCoord,
    val tier: TileTier,
    val groupSize: Int,
    val resultingTier: TileTier,
    val explanation: String
)

data class SparkleParticle(
    val id: String,
    val coord: HexCoord,
    val color: Color,
    val angleRad: Float,
    val distanceFactor: Float,
    val radiusDp: Float
)

enum class RewardKind {
    HINT,
    EXTRA_MOVES
}

enum class BreakType {
    REWARDED,
    INTERSTITIAL
}

data class BreakModalState(
    val isOpen: Boolean = false,
    val type: BreakType = BreakType.REWARDED,
    val rewardKind: RewardKind? = null
)

@Serializable
data class UserProgress(
    val unlockedLevel: Int = 1,
    val stars: Map<Int, Int> = emptyMap(),
    val highScores: Map<Int, Int> = emptyMap(),
    val coins: Int = 100,
    val hintsRemaining: Int = 3,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val soundVolume: Float = 0.8f,
    val musicVolume: Float = 0.5f
) {
    val totalStars: Int
        get() = stars.values.sum()
}
