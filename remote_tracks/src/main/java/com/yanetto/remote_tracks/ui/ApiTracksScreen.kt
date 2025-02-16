package com.yanetto.remote_tracks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanetto.common_tracks.presentation.TracksScreen
import com.yanetto.common_tracks.presentation.TracksUiState

@Composable
internal fun ApiTracksScreen(
    modifier: Modifier = Modifier,
    navigateToPlayer: () -> Unit,
    viewModel: ApiTracksScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    TracksScreen(
        modifier = modifier,
        uiState = uiState,
        onSearchTracks = { viewModel.searchTracks(it) },
        onLoadTracks = { viewModel.loadTracks() },
        onTrackClick = {
            viewModel.setAndPlayPlaylist(
                (uiState as? TracksUiState.Success)?.tracks ?: emptyList(),
                it
            )
        },
        navigateToPlayer = navigateToPlayer,
        onPlayPauseClick = { viewModel.changePlayingState() },
        loadNext = { viewModel.loadNext() },
        isCurrentPlayingTrack = { viewModel.isCurrentPlayingTrack(it) }
    )
}