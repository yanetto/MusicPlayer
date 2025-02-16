package com.yanetto.music_player.domain

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.yanetto.common_model.model.Track
import com.yanetto.music_player.presentation.MusicPlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerState (
    val tracks: List<Track> = emptyList(),
    val currentMediaItem: MediaItem? = null,
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val duration: Long = 0L,
    val isSliderMoving: Boolean = false
)

@Singleton
class MusicPlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaController: MediaController? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        startMusicService()
        initializeMediaController()
        updateProgress()
    }

    private fun initializeMediaController() {
        if (mediaController != null) return

        val sessionToken = SessionToken(context, ComponentName(context, MusicPlayerService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            _playerState.update { it.copy(isPlaying = playbackState == Player.STATE_READY && mediaController?.playWhenReady == true) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _playerState.update {
                it.copy(
                    currentIndex = mediaController?.currentMediaItemIndex ?: 0,
                    currentMediaItem = mediaController?.currentMediaItem
                ) }
            updateDuration()
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (player.playbackState == Player.STATE_READY) {
                _playerState.update { it.copy(isPlaying = player.isPlaying) }
            }
            _playerState.update {
                it.copy(
                    currentIndex = player.currentMediaItemIndex,
                    currentMediaItem = mediaController?.currentMediaItem
                ) }
            updateDuration()
        }
    }

    private fun updateProgress() {
        scope.launch {
            while (true) {
                if (_playerState.value.isPlaying && !_playerState.value.isSliderMoving) {
                    val currentPosition = mediaController?.currentPosition ?: 0L
                    val trackDuration = _playerState.value.duration.takeIf { it > 0 } ?: 1L
                    _playerState.update { it.copy(progress = currentPosition.toFloat() / trackDuration) }
                }
                delay(200)
            }
        }
    }

    fun setPlaylist(playlist: List<Track>, startIndex: Int = 0) {
        if (playlist == _playerState.value.tracks && _playerState.value.currentIndex == startIndex) {
            if (_playerState.value.isPlaying) pause()
            else play()
            return
        }
        _playerState.update {
            it.copy(tracks = playlist.toList())
        }
        _playerState.update {
            it.copy(
                currentIndex = startIndex,
                currentMediaItem = playlist[startIndex].toMediaItem()
            ) }

        mediaController?.apply {
            setMediaItems(playlist.toMediaItemList())
            prepare()
            seekTo(startIndex, 0)
            play()
        }
    }

    private fun playTrack(index: Int) {
        if (index in _playerState.value.tracks.indices) {
            _playerState.update {
                it.copy(
                    currentIndex = index,
                    currentMediaItem = _playerState.value.tracks[index].toMediaItem()
                ) }
            mediaController?.seekTo(index, 0)
            mediaController?.play()
        }
    }

    fun nextTrack() {
        if (_playerState.value.currentIndex < _playerState.value.tracks.lastIndex) {
            playTrack(_playerState.value.currentIndex + 1)
        }
    }

    fun prevTrack() {
        if (_playerState.value.currentIndex > 0) {
            playTrack(_playerState.value.currentIndex - 1)
        }
    }

    fun play() = mediaController?.play()
    fun pause() = mediaController?.pause()

    fun seekTo(position: Long) {
        _playerState.update { it.copy(isSliderMoving = false) }
        mediaController?.seekTo(position)
        val trackDuration = _playerState.value.duration.takeIf { it > 0 } ?: 1L
        _playerState.update { it.copy(progress = position.toFloat() / trackDuration) }
    }

    fun updateSliderMoving(isMoving: Boolean) {
        _playerState.update { it.copy(isSliderMoving = isMoving) }
    }

    private fun updateDuration() {
        scope.launch {
            var newDuration = mediaController?.duration ?: C.TIME_UNSET
            if (newDuration == C.TIME_UNSET) {
                delay(200)
                newDuration = mediaController?.duration ?: 0L
            }
            _playerState.update { it.copy(duration = maxOf(newDuration, 0L)) }
        }
    }

    private fun startMusicService() {
        val intent = Intent(context, MusicPlayerService::class.java)
        context.startForegroundService(intent)
    }
}

fun Track.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(this.id.toString())
        .setUri(this.filePath)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(this.title)
                .setArtist(this.artist)
                .setArtworkUri(this.albumCoverUri?.toUri())
                .build()
        )
        .build()
}


fun List<Track>.toMediaItemList(): List<MediaItem> = this.map { it.toMediaItem() }