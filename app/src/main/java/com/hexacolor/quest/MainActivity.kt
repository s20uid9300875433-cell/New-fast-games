package com.hexacolor.quest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.hexacolor.quest.audio.SoundSynthesizer
import com.hexacolor.quest.data.ProgressRepository
import com.hexacolor.quest.navigation.GameRoute
import com.hexacolor.quest.navigation.HomeRoute
import com.hexacolor.quest.navigation.LevelSelectRoute
import com.hexacolor.quest.ui.screens.GameScreen
import com.hexacolor.quest.ui.screens.HomeScreen
import com.hexacolor.quest.ui.screens.LevelSelectScreen
import com.hexacolor.quest.ui.theme.HexaColorQuestTheme
import com.hexacolor.quest.viewmodel.AppViewModel
import com.hexacolor.quest.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            HexaColorQuestTheme {
                HexaColorQuestApp()
            }
        }
    }
}

@Composable
fun HexaColorQuestApp() {
    val context = LocalContext.current.applicationContext
    val progressRepository = remember { ProgressRepository(context) }
    val soundSynthesizer = remember { SoundSynthesizer(context) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, soundSynthesizer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> soundSynthesizer.onAppResumed()
                Lifecycle.Event.ON_PAUSE -> soundSynthesizer.onAppPaused()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            soundSynthesizer.onAppPaused()
        }
    }

    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModel.Factory(progressRepository, soundSynthesizer)
    )
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(progressRepository, soundSynthesizer)
    )

    val progress by appViewModel.progress.collectAsStateWithLifecycle()
    val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                progress = progress,
                onPlayCurrentLevel = {
                    navController.navigate(GameRoute(levelId = progress.unlockedLevel))
                },
                onOpenLevelSelect = {
                    navController.navigate(LevelSelectRoute)
                },
                onToggleSound = appViewModel::toggleSound,
                onToggleMusic = appViewModel::toggleMusic,
                onToggleHaptics = appViewModel::toggleHaptics,
                onChangeSoundVolume = appViewModel::setSoundVolume,
                onChangeMusicVolume = appViewModel::setMusicVolume,
                onResetProgress = appViewModel::resetProgress,
                onPlayClick = appViewModel::playClick
            )
        }

        composable<LevelSelectRoute> {
            LevelSelectScreen(
                progress = progress,
                onSelectLevel = { selectedId ->
                    navController.navigate(GameRoute(levelId = selectedId))
                },
                onBack = {
                    navController.popBackStack()
                },
                onPlayClick = appViewModel::playClick
            )
        }

        composable<GameRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<GameRoute>()
            GameScreen(
                levelId = route.levelId,
                uiState = gameUiState,
                progress = progress,
                gameViewModel = gameViewModel,
                onExitToLevelSelect = {
                    val popped = navController.popBackStack(LevelSelectRoute, inclusive = false)
                    if (!popped) {
                        navController.navigate(LevelSelectRoute) {
                            popUpTo(HomeRoute)
                        }
                    }
                },
                onAdvanceToLevel = { nextLevelId ->
                    navController.navigate(GameRoute(levelId = nextLevelId)) {
                        popUpTo<GameRoute> { inclusive = true }
                    }
                },
                onToggleSound = appViewModel::toggleSound,
                onToggleMusic = appViewModel::toggleMusic,
                onToggleHaptics = appViewModel::toggleHaptics,
                onChangeSoundVolume = appViewModel::setSoundVolume,
                onChangeMusicVolume = appViewModel::setMusicVolume,
                onResetProgress = appViewModel::resetProgress
            )
        }
    }
}
