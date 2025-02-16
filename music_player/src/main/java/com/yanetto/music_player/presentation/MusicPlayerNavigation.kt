package com.yanetto.music_player.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yanetto.music_player.ui.MusicPlayerScreen

const val MUSIC_PLAYER_ROUTE = "player_screen"

fun NavController.navigateToMusicPlayer() = navigate(MUSIC_PLAYER_ROUTE) {
    launchSingleTop = true
}

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