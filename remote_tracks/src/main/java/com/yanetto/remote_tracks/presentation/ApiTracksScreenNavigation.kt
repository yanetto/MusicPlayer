package com.yanetto.remote_tracks.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val REMOTE_TRACKS_ROUTE = "remote_tracks_screen"

fun NavGraphBuilder.remoteTracksScreen(
    navigateToPlayer: () -> Unit
) {
    composable(route = REMOTE_TRACKS_ROUTE) {
        ApiTracksScreen(navigateToPlayer = navigateToPlayer)
    }
}