package com.yanetto.music_player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.yanetto.music_player.domain.MusicPlayerController
import com.yanetto.music_player.domain.toMediaItem
import com.yanetto.music_player.domain.toMediaItemList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MusicPlayerState(
    val isSliderMoving: Boolean = false,
    val tracks: List<MediaItem> = listOf(),
    val currentIndex: Int = 0,
    val currentTrack: MediaItem? = tracks.firstOrNull(),
    val mediaController: MediaController? = null,
    val isPlaying: Boolean = true,
    val progress: Float = 0f,
    val duration: Long = 0L
)

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicPlayer: MusicPlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicPlayerState())
    val uiState = _uiState.asStateFlow()

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            musicPlayer.tracks.collect { tracks ->
                _uiState.update { it.copy(tracks = tracks.toMediaItemList()) }
            }
        }

        viewModelScope.launch {
            musicPlayer.currentIndex.collect { index ->
                _uiState.update {
                    it.copy(
                        currentIndex = index,
                        currentTrack = musicPlayer.tracks.value.getOrNull(index)?.toMediaItem()
                    )
                }
            }
        }

        viewModelScope.launch {
            musicPlayer.progress.collect { progress ->
                if (!_uiState.value.isSliderMoving) {
                    _uiState.update { it.copy(progress = progress) }
                }
            }
        }

        viewModelScope.launch {
            musicPlayer.duration.collect { duration ->
                _uiState.update { it.copy(duration = duration) }
            }
        }

        viewModelScope.launch {
            musicPlayer.isPlaying.collect { isPlaying ->
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
        }
    }

    fun play() = musicPlayer.play()
    fun pause() = musicPlayer.pause()
    fun nextTrack() = musicPlayer.nextTrack()
    fun prevTrack() = musicPlayer.prevTrack()

    fun updateSliderMoving(isMoving: Boolean) {
        _uiState.update { it.copy(isSliderMoving = isMoving) }
        musicPlayer.updateSliderMoving(isMoving)
    }

    fun seekTo(newPosition: Long) {
        _uiState.update { currentState ->
            currentState.copy(progress = newPosition.toFloat() / (_uiState.value.duration.takeIf { it > 0 } ?: 1))
        }
        musicPlayer.seekTo(newPosition)
    }
}

