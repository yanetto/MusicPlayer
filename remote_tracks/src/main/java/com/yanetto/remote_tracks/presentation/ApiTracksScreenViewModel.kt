package com.yanetto.remote_tracks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanetto.common_model.model.Track
import com.yanetto.common_tracks.presentation.TracksUiState
import com.yanetto.music_player.domain.MusicPlayerController
import com.yanetto.remote_tracks.data.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiTracksScreenViewModel @Inject constructor(
    private val repository: ApiRepository,
    private val musicPlayer: MusicPlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow<TracksUiState>(TracksUiState.Loading)
    val uiState: StateFlow<TracksUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTracks()
        observeCurrentTrack()
    }

    fun loadTracks() {
        viewModelScope.launch {
            _uiState.value = TracksUiState.Loading
            runCatching {
                repository.getTracks()
            }.onSuccess { tracks ->
                if (tracks.isNotEmpty()) {
                    _uiState.value = TracksUiState.Success(tracks, getCurrentTrack())
                } else {
                    _uiState.value = TracksUiState.Empty
                }
            }.onFailure { error ->
                _uiState.value = TracksUiState.Error(error.message)
            }
        }
    }

    fun searchTracks(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = TracksUiState.Loading
            runCatching {
                repository.searchTracks(query)
            }.onSuccess { tracks ->
                if (tracks.isNotEmpty()) {
                    _uiState.value = TracksUiState.Success(tracks, getCurrentTrack())
                } else {
                    _uiState.value = TracksUiState.Empty
                }
            }.onFailure { error ->
                _uiState.value = TracksUiState.Error(error.message)
            }
        }
    }

    fun playPlaylist(playlist: List<Track>, startIndex: Int) {
        musicPlayer.setPlaylist(playlist, startIndex)
    }

    private fun observeCurrentTrack() {
        viewModelScope.launch {
            musicPlayer.currentIndex.collect { _ ->
                updateCurrentTrack()
            }
        }
    }

    private fun updateCurrentTrack() {
        val currentState = _uiState.value
        if (currentState is TracksUiState.Success) {
            _uiState.value = currentState.copy(currentTrack = getCurrentTrack())
        }
    }

    private fun getCurrentTrack(): Track? {
        return musicPlayer.tracks.value.getOrNull(musicPlayer.currentIndex.value)
    }
}