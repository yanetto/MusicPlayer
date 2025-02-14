package com.yanetto.musicplayer.ui.navigation

import com.yanetto.musicplayer.R

sealed class Screen(val route: String, val title: String, val resourceId: Int) {
    data object LocalTracks : Screen("local_tracks_screen", "Скачанные треки", R.drawable.local_tracks)
    data object RemoteTracks : Screen("remote_tracks_screen", "Треки из API", R.drawable.remote_tracks)
    data object PlayerScreen : Screen("player_screen", "Плеер", 0)
}
