package com.yanetto.musicplayer.ui.navigation

import com.yanetto.musicplayer.R

sealed class Screen(val route: String, val titleResId: Int, val imageResId: Int?) {
    data object LocalTracks :
        Screen("local_tracks_screen", R.string.downloaded_tracks, R.drawable.local_tracks)

    data object RemoteTracks :
        Screen("remote_tracks_screen", R.string.api_tracks, R.drawable.remote_tracks)

    data object PlayerScreen : Screen("player_screen", R.string.player, null)
}
