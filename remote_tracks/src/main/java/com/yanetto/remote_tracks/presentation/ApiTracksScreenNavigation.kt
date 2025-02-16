package com.yanetto.remote_tracks.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yanetto.remote_tracks.ui.ApiTracksScreen

const val REMOTE_TRACKS_ROUTE = "remote_tracks_screen"

fun NavGraphBuilder.remoteTracksScreen(
    navigateToPlayer: () -> Unit
) {
    composable(
        route = REMOTE_TRACKS_ROUTE,
        enterTransition = { fadeIn(animationSpec = tween(durationMillis = 1200)) },
        exitTransition = { fadeOut(animationSpec = tween(durationMillis = 800)) }
    ) {
        ApiTracksScreen(navigateToPlayer = navigateToPlayer)
    }
}