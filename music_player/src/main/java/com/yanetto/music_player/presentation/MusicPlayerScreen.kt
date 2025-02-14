package com.yanetto.music_player.presentation

import androidx.annotation.OptIn
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.yanetto.music_player.R
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class)
@Composable
fun MusicPlayerScreen(
    viewModel: MusicViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val musicPlayerState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val image = musicPlayerState.currentTrack?.mediaMetadata?.artworkUri

        Row(
            modifier = Modifier.weight(0.5f).fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { navigateBack() }) {
                Icon(
                    painter = painterResource(R.drawable.back),
                    contentDescription = stringResource(R.string.close),
                    modifier = Modifier.size(40.dp)
                )
            }

        }

        Box(
            modifier = Modifier.padding(horizontal = 16.dp).weight(3f),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                AsyncImage(
                    model = image,
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

        Spacer(modifier = Modifier.weight(0.4f))

        Text(
            text = musicPlayerState.currentTrack?.mediaMetadata?.title.toString(),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp).weight(0.2f)
        )
        Text(
            text = musicPlayerState.currentTrack?.mediaMetadata?.artist.toString(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp).weight(0.2f)
        )

        Spacer(modifier = Modifier.weight(0.4f))


        var sliderPosition by remember { mutableFloatStateOf(musicPlayerState.progress) }

        LaunchedEffect(musicPlayerState.progress) {
            if (sliderPosition != musicPlayerState.progress) {
                sliderPosition = musicPlayerState.progress
            }
        }

        Slider(
            value = sliderPosition,
            onValueChange = { newProgress ->
                viewModel.updateSliderMoving(true)
                sliderPosition = newProgress
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


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(0.5f)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime((sliderPosition * musicPlayerState.duration).toLong()),
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(formatTime(musicPlayerState.duration), modifier = Modifier.padding(start = 8.dp))
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.prevTrack() },
                enabled = musicPlayerState.currentIndex != 0
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_previous),
                    contentDescription = stringResource(R.string.previous_track),
                    modifier = Modifier.size(40.dp)
                )
            }
            IconButton(onClick = {
                if (musicPlayerState.isPlaying) viewModel.pause() else viewModel.play()
            }) {
                Icon(
                    painter = if (musicPlayerState.isPlaying) painterResource(R.drawable.pause) else painterResource(
                        R.drawable.play
                    ),
                    contentDescription = stringResource(R.string.play_pause),
                    modifier = Modifier.size(40.dp)
                )
            }
            IconButton(
                onClick = { viewModel.nextTrack() },
                enabled = musicPlayerState.currentIndex != musicPlayerState.tracks.size - 1
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    contentDescription = stringResource(R.string.next_track),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds)
}
