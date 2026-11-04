package com.hexacolor.quest.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object LevelSelectRoute

@Serializable
data class GameRoute(val levelId: Int)
