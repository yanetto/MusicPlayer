package com.yanetto.music_player.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val MUSIC_PLAYER_ROUTE = "player_screen"

fun NavGraphBuilder.musicPlayerScreen(
    navigateBack: () -> Unit
) {
    composable(
        route = MUSIC_PLAYER_ROUTE,
        enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(durationMillis = 800)) },
        exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(durationMillis = 800)) }
    ) {
        MusicPlayerScreen(navigateBack = navigateBack)
    }
}