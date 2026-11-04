package com.hexacolor.quest

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.hexacolor.quest.data.LevelRepository
import com.hexacolor.quest.logic.GameEngine
import com.hexacolor.quest.logic.HexMath
import com.hexacolor.quest.logic.HintSolver
import com.hexacolor.quest.logic.MoveResult
import com.hexacolor.quest.model.HexCoord
import com.hexacolor.quest.model.LevelConfig
import com.hexacolor.quest.model.LevelObjective
import com.hexacolor.quest.model.ObjectiveType
import com.hexacolor.quest.model.StarThresholds
import com.hexacolor.quest.model.TileTier
import com.hexacolor.quest.ui.theme.HexaColorQuestTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HexGameRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appNameMatchesMetadata() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Hexa Color Quest", context.getString(R.string.app_name))
    }

    @Test
    fun all100LevelsGeneratedWithValidDifficultyBrackets() {
        val levels = LevelRepository.getAllLevels()
        assertEquals(100, levels.size)
        assertEquals("Ocean Pearl", levels.first().name)
        assertEquals("Normal", levels[0].difficulty)
        assertEquals("Hard", levels[9].difficulty)
        assertEquals("Hard+", levels[19].difficulty)
        assertEquals("Very Hard", levels[39].difficulty)
        assertEquals("Expert", levels[59].difficulty)
        assertEquals("Master", levels[99].difficulty)
    }

    @Test
    fun threeTileSlideMergeSynthesizesNextTierAndCascadeWorks() {
        val level = LevelConfig(
            id = 1,
            chapter = 1,
            name = "Cascade Test",
            difficulty = "Normal",
            radius = 2,
            initialTiles = listOf(
                HexCoord(-1, 0) to TileTier.TIER_1,
                HexCoord(0, -1) to TileTier.TIER_1,
                HexCoord(1, 0) to TileTier.TIER_1,
                HexCoord(0, 1) to TileTier.TIER_2,
                HexCoord(-1, 1) to TileTier.TIER_2
            ),
            moveLimit = 5,
            objective = LevelObjective(
                type = ObjectiveType.REACH_TIER,
                targetTier = TileTier.TIER_3,
                description = "Reach Tier 3"
            ),
            starThresholds = StarThresholds(0, 2, 4)
        )

        val engine = GameEngine(level)
        val result = engine.moveTile(HexCoord(-1, 0), HexCoord(0, 0))

        assertTrue(result is MoveResult.Merged)
        val merged = result as MoveResult.Merged
        assertEquals(TileTier.TIER_3, merged.mergedTile.tier)
        assertEquals(2, merged.comboCount)
        assertTrue(engine.isObjectiveMet())
        assertEquals(4, engine.movesLeft)

        // Undo restores initial 5 tiles
        assertTrue(engine.undo())
        assertEquals(5, engine.getTiles().size)
        assertEquals(5, engine.movesLeft)
        assertFalse(engine.isObjectiveMet())
    }

    @Test
    fun hintSolverFindsImmediateMergeMove() {
        val level = LevelRepository.getLevel(1)
        val engine = GameEngine(level)
        val validCoords = HexMath.generateGrid(level.radius, level.blockedCoords)
        val suggestion = HintSolver.findBestMoveHint(
            validCoords = validCoords,
            currentTiles = engine.getTiles(),
            trayBlocks = engine.trayBlocks
        )
        assertNotNull(suggestion)
    }

    @Test
    fun homeToGameScreenNavigationCujRendersBoardAndTray() {
        composeTestRule.setContent {
            HexaColorQuestTheme {
                HexaColorQuestApp()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_play_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_play_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hexagon_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("block_tray").assertIsDisplayed()
        composeTestRule.onNodeWithTag("game_hint_button").assertIsDisplayed()
    }
}
