package com.yanetto.common_tracks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanetto.common_model.model.Track
import com.yanetto.common_model.repository.TracksRepository
import com.yanetto.music_player.domain.MusicPlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class TracksScreenViewModel(
    private val repository: TracksRepository,
    private val musicPlayer: MusicPlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow<TracksUiState>(TracksUiState.Loading)
    val uiState: StateFlow<TracksUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTracks()
        observeCurrentTrack()
    }

    fun onRestart() {
        _uiState.update {
            TracksUiState.NotStarted
        }
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
            if (uiState.value is TracksUiState.Success) {
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
    }

    fun setAndPlayPlaylist(playlist: List<Track>, startIndex: Int) {
        musicPlayer.setPlaylist(playlist, startIndex)
    }

    private fun observeCurrentTrack() {
        viewModelScope.launch {
            musicPlayer.playerState.collect { newState ->
                _uiState.update { currentState ->
                    if (currentState is TracksUiState.Success) {
                        currentState.copy(
                            currentTrack = newState.tracks.getOrNull(newState.currentIndex),
                            isPlay = musicPlayer.playerState.value.isPlaying
                        )
                    } else {
                        currentState
                    }
                }
            }
        }
    }

    fun isCurrentPlayingTrack(track: Track): Boolean {
        return track.id == musicPlayer.playerState.value.currentTrack?.id
    }

    private fun getCurrentTrack(): Track? {
        return musicPlayer.playerState.value.tracks.getOrNull(musicPlayer.playerState.value.currentIndex)
    }

    private fun isPlaying(): Boolean {
        return musicPlayer.playerState.value.isPlaying
    }

    fun changePlayingState() {
        if (musicPlayer.playerState.value.isPlaying) musicPlayer.pause()
        else musicPlayer.play()
    }
}
