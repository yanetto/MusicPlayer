package com.yanetto.music_player.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val MUSIC_PLAYER_ROUTE = "player_screen"

fun NavGraphBuilder.musicPlayerScreen(
    navigateBack: () -> Unit
) {
    composable(
        route = MUSIC_PLAYER_ROUTE
    ) {
        MusicPlayerScreen(navigateBack = navigateBack)
    }
}