package com.yanetto.music_player.ui

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.yanetto.music_player.R
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
@Composable
internal fun MusicPlayerScreen(
    viewModel: MusicViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val musicPlayerState by viewModel.uiState.collectAsState()

    var sliderPosition by remember { mutableFloatStateOf(musicPlayerState.progress) }

    LaunchedEffect(musicPlayerState.progress) {
        if (sliderPosition != musicPlayerState.progress) {
            sliderPosition = musicPlayerState.progress
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(musicPlayerState.currentTrack?.mediaMetadata?.albumTitle.toString(), navigateBack)

        AlbumCover(musicPlayerState.currentTrack?.mediaMetadata?.artworkUri)

        Spacer(modifier = Modifier.weight(0.4f))

        TrackDetails(
            title = musicPlayerState.currentTrack?.mediaMetadata?.title.toString(),
            artist = musicPlayerState.currentTrack?.mediaMetadata?.artist.toString()
        )

        Spacer(modifier = Modifier.weight(0.4f))

        Slider(
            value = sliderPosition,
            onValueChange = { newProgress ->
                sliderPosition = newProgress
                viewModel.updateSliderMoving(true)
            },
            onValueChangeFinished = {
                viewModel.updateSliderMoving(false)
                val newPosition = (sliderPosition * musicPlayerState.duration).toLong()
                viewModel.seekTo(newPosition)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(0.5f)
        )

        TrackTimeDisplay(
            currentTime = (sliderPosition * musicPlayerState.duration).toLong(),
            duration = musicPlayerState.duration
        )

        PlaybackControls(
            isPlaying = musicPlayerState.isPlaying,
            onPlayPause = { if (musicPlayerState.isPlaying) viewModel.pause() else viewModel.play() },
            onNext = { viewModel.nextTrack() },
            onPrev = { viewModel.prevTrack() },
            isPrevEnabled = musicPlayerState.currentIndex != 0,
            isNextEnabled = musicPlayerState.currentIndex != musicPlayerState.tracks.size - 1
        )
    }
}


@Composable
fun ColumnScope.TopBar(
    albumTitle: String,
    navigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(0.7f)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigateBack() }) {
                Icon(
                    painter = painterResource(R.drawable.back),
                    contentDescription = stringResource(R.string.close),
                    modifier = Modifier.size(40.dp)
                )
            }

        }
        Text(
            text = stringResource(R.string.album_title, albumTitle),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun ColumnScope.AlbumCover(imageUri: Uri?) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .weight(3f),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(R.string.album_cover),
                modifier = Modifier
                    .size(350.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(R.drawable.default_album_cover),
                contentDescription = stringResource(R.string.album_cover),
                modifier = Modifier
                    .size(350.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ColumnScope.TrackDetails(title: String?, artist: String?) {
    Text(
        text = title.orEmpty(),
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .weight(0.2f)
    )
    Text(
        text = artist.orEmpty(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .weight(0.2f)
    )
}

@Composable
fun ColumnScope.TrackTimeDisplay(currentTime: Long, duration: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .weight(0.5f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            formatTime(currentTime),
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(formatTime(duration), modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun ColumnScope.PlaybackControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    isPrevEnabled: Boolean,
    isNextEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .weight(1f),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onPrev() },
            enabled = isPrevEnabled
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_previous),
                contentDescription = stringResource(R.string.previous_track),
                modifier = Modifier.size(40.dp)
            )
        }

        IconButton(onClick = onPlayPause) {
            Icon(
                painter = if (isPlaying) painterResource(R.drawable.pause) else painterResource(R.drawable.play),
                contentDescription = stringResource(R.string.play_pause),
                modifier = Modifier.size(40.dp)
            )
        }

        IconButton(
            onClick = { onNext() },
            enabled = isNextEnabled
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_next),
                contentDescription = stringResource(R.string.next_track),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}


fun formatTime(ms: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds)
}
