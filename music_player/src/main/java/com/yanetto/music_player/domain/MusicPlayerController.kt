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
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaController: MediaController? = null

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    private val _currentIndex = MutableStateFlow(-1)
    private val _isPlaying = MutableStateFlow(false)
    private val _progress = MutableStateFlow(0f)
    private val _duration = MutableStateFlow(0L)
    private val _isSliderMoving = MutableStateFlow(false)

    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    val progress: StateFlow<Float> = _progress.asStateFlow()
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        startMusicService()
        initializeMediaController()
        startProgressUpdater()
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
            _isPlaying.value = playbackState == Player.STATE_READY && mediaController?.playWhenReady == true
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentIndex.value = mediaController?.currentMediaItemIndex ?: 0
            updateDurationWithRetry()
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (player.playbackState == Player.STATE_READY) {
                _isPlaying.value = player.isPlaying
            }
            _currentIndex.value = player.currentMediaItemIndex
            updateDurationWithRetry()
        }

    }

    private fun startProgressUpdater() {
        scope.launch {
            while (true) {
                if (_isPlaying.value && !_isSliderMoving.value) {
                    val currentPosition = mediaController?.currentPosition ?: 0L
                    val trackDuration = _duration.value.takeIf { it > 0 } ?: 1L
                    _progress.value = currentPosition.toFloat() / trackDuration
                }
                delay(200)
            }
        }
    }

    fun setPlaylist(playlist: List<Track>, startIndex: Int = 0) {
        if (playlist == _tracks.value && _currentIndex.value == startIndex) {
            if (_isPlaying.value) pause()
            else play()
            return
        }
        _tracks.value = playlist
        _currentIndex.value = startIndex

        mediaController?.apply {
            setMediaItems(playlist.toMediaItemList())
            prepare()
            seekTo(startIndex, 0)
            play()
        }
    }

    private fun playTrack(index: Int) {
        if (index in _tracks.value.indices) {
            _currentIndex.value = index
            mediaController?.seekTo(index, 0)
            mediaController?.play()
        }
    }

    fun nextTrack() {
        if (_currentIndex.value < _tracks.value.lastIndex) {
            playTrack(_currentIndex.value + 1)
        }
    }

    fun prevTrack() {
        if (_currentIndex.value > 0) {
            playTrack(_currentIndex.value - 1)
        }
    }

    fun play() = mediaController?.play()
    fun pause() = mediaController?.pause()

    fun seekTo(position: Long) {
        _isSliderMoving.value = false
        mediaController?.seekTo(position)
        val trackDuration = _duration.value.takeIf { it > 0 } ?: 1L
        _progress.value = position.toFloat() / trackDuration
    }

    fun updateSliderMoving(isMoving: Boolean) {
        _isSliderMoving.value = isMoving
    }

    private fun updateDurationWithRetry() {
        scope.launch {
            var newDuration = mediaController?.duration ?: C.TIME_UNSET
            if (newDuration == C.TIME_UNSET) {
                delay(200)
                newDuration = mediaController?.duration ?: 0L
            }
            _duration.value = maxOf(newDuration, 0L)
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