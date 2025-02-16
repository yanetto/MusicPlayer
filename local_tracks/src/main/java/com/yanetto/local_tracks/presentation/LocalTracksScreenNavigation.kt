package com.yanetto.local_tracks.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yanetto.local_tracks.ui.LocalTracksScreenCheckPermission

const val LOCAL_TRACKS_ROUTE = "local_tracks_screen"

fun NavGraphBuilder.localTracksScreen(
    navigateToPlayer: () -> Unit
) {
    composable(
        route = LOCAL_TRACKS_ROUTE,
        enterTransition = { fadeIn(animationSpec = tween(durationMillis = 1200)) },
        exitTransition = { fadeOut(animationSpec = tween(durationMillis = 800)) }
    ) {
        LocalTracksScreenCheckPermission { navigateToPlayer() }
    }
}

