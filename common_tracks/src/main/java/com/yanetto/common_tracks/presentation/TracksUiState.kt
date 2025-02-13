package com.yanetto.common_tracks.presentation

import com.yanetto.common_model.model.Track

sealed class TracksUiState {
    data object Loading : TracksUiState()
    data class Success(val tracks: List<Track>, val currentTrack: Track?) : TracksUiState()
    data object Empty : TracksUiState()
    data class Error(val message: String?) : TracksUiState()
}
