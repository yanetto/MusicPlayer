package com.yanetto.local_tracks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanetto.common_model.model.Track
import com.yanetto.common_tracks.presentation.TracksUiState
import com.yanetto.local_tracks.data.repository.MediaStoreTracksRepository
import com.yanetto.music_player.domain.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalTracksScreenViewModel @Inject constructor(
    private val repository: MediaStoreTracksRepository,
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
        viewModelScope.launch(Dispatchers.IO) {

            _uiState.update { TracksUiState.Loading }

            runCatching { repository.getTracks() }
                .onSuccess { tracks ->
                    if (tracks.isNotEmpty()) {
                        _uiState.update {
                            TracksUiState.Success(
                                tracks = tracks,
                                currentTrack = getCurrentTrack(),
                                isPlay = isPlaying(),
                                isLoadingNext = false
                            )
                        }
                    } else {
                        _uiState.update { TracksUiState.Empty }
                    }
                }
                .onFailure { error ->
                    _uiState.update { TracksUiState.Error(error.message) }
                }
        }
    }

    fun searchTracks(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {

            _uiState.update { TracksUiState.Loading }

            runCatching { repository.searchTracks(query) }
                .onSuccess { tracks ->
                    if (tracks.isNotEmpty()) {
                        _uiState.update {
                            TracksUiState.Success(
                                tracks = tracks,
                                currentTrack = getCurrentTrack(),
                                isPlay = isPlaying(),
                                isLoadingNext = false
                            )
                        }
                    } else {
                        _uiState.update { TracksUiState.Empty }
                    }
                }
                .onFailure { error ->
                    _uiState.update { TracksUiState.Error(error.message) }
                }
        }
    }

    fun loadNext() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!(_uiState.value as TracksUiState.Success).isLoadingNext) {

                _uiState.update {
                    TracksUiState.Success(
                        tracks = (_uiState.value as TracksUiState.Success).tracks,
                        currentTrack = getCurrentTrack(),
                        isPlay = isPlaying(),
                        isLoadingNext = true
                    )
                }

                runCatching { repository.loadNext() }
                    .onSuccess { tracks ->
                        if (tracks.isNotEmpty()) {
                            _uiState.update {
                                TracksUiState.Success(
                                    tracks = tracks,
                                    currentTrack = getCurrentTrack(),
                                    isPlay = isPlaying(),
                                    isLoadingNext = false
                                )
                            }
                        } else {
                            _uiState.update { TracksUiState.Empty }
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { TracksUiState.Error(error.message) }
                    }
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

        viewModelScope.launch {
            musicPlayer.tracks.collect { _ ->
                updateCurrentTrack()
            }
        }

        viewModelScope.launch {
            musicPlayer.isPlaying.collect { _ ->
                updatePlayingState()
            }
        }
    }

    private fun updateCurrentTrack() {
        _uiState.update { currentState ->
            if (currentState is TracksUiState.Success) {
                currentState.copy(currentTrack = getCurrentTrack())
            } else {
                currentState
            }
        }
    }

    private fun updatePlayingState() {
        _uiState.update { currentState ->
            if (currentState is TracksUiState.Success) {
                currentState.copy(isPlay = musicPlayer.isPlaying.value)
            } else {
                currentState
            }
        }
    }

    private fun getCurrentTrack(): Track? {
        return musicPlayer.tracks.value.getOrNull(musicPlayer.currentIndex.value)
    }

    private fun isPlaying(): Boolean {
        return musicPlayer.isPlaying.value
    }

    fun changePlayingState() {
        if (musicPlayer.isPlaying.value) musicPlayer.pause()
        else musicPlayer.play()
    }
}
