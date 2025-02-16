package com.yanetto.common_tracks.presentation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yanetto.common_model.model.Track
import com.yanetto.common_tracks.R

const val LIMIT = 25
const val DIFF = 5

@Composable
fun TracksScreen(
    modifier: Modifier = Modifier,
    uiState: TracksUiState,
    onSearchTracks: (String) -> Unit,
    onLoadTracks: () -> Unit,
    onTrackClick: (Int) -> Unit,
    onPlayPauseClick: () -> Unit,
    navigateToPlayer: () -> Unit,
    isCurrentPlayingTrack: (Track) -> Boolean,
    loadNext: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(listState.firstVisibleItemIndex) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (lastVisibleIndex != null) {
            if (listState.layoutInfo.totalItemsCount >= LIMIT && lastVisibleIndex >= listState.layoutInfo.totalItemsCount - DIFF) {
                Log.d("LOAD_NEXT", listState.layoutInfo.totalItemsCount.toString() + " " + lastVisibleIndex)
                loadNext()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        SearchField(
            query = searchQuery,
            onValueChange = {
                searchQuery = it
                if (it.isNotBlank()) {
                    onSearchTracks(it)
                } else {
                    onLoadTracks()
                }
            },
            focusManager = focusManager,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when (uiState) {
            is TracksUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TracksUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = listState
                    ) {
                        items(uiState.tracks) { track ->
                            TrackItem(
                                track = track,
                                isPlaying = false,
                                isCurrentTrack = isCurrentPlayingTrack(track)
                            ) {
                                onTrackClick(uiState.tracks.indexOf(track))
                                if (track != uiState.currentTrack) navigateToPlayer()
                            }
                        }
                        if (uiState.isLoadingNext) {
                            item {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                    if ((uiState as? TracksUiState.Success)?.currentTrack != null) {
                        val track = (uiState as? TracksUiState.Success)?.currentTrack
                        val isPause = !(uiState as? TracksUiState.Success)?.isPlay!!
                        TrackItem(
                            track = track!!,
                            isPlaying = true,
                            isPause = isPause,
                            onPlayPauseClick = onPlayPauseClick,
                            onItemClick = { navigateToPlayer() }
                        )

                    }
                }
            }
            is TracksUiState.Error -> {
                val message = uiState.message
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = message ?: stringResource(R.string.unknown_error_occurred), modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                }
            }
            is TracksUiState.Empty -> {
                EmptyState()
            }
        }

        (uiState as? TracksUiState.Success)?.let { state ->
            state.currentTrack?.let { track ->
                TrackItem(
                    isPlaying = true,
                    isPause = !state.isPlay,
                    track = track,
                    onPlayPauseClick = onPlayPauseClick,
                    onItemClick = { navigateToPlayer() }
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.nothing_found),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SearchField(
    query: String,
    onValueChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        shape = CircleShape,
        value = query,
        onValueChange = onValueChange,
        placeholder = { Text(stringResource(R.string.track_search)) },
        keyboardActions = KeyboardActions { focusManager.clearFocus() },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        leadingIcon = { Icon(painterResource(R.drawable.search), contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onValueChange("")
                            focusManager.clearFocus()
                        }
                )
            }
        }
    )
}

@Composable
fun TrackItem(
    isCurrentTrack: Boolean = false,
    isPlaying: Boolean,
    isPause: Boolean = false,
    onPlayPauseClick: () -> Unit = {},
    track: Track,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        colors = if (isPlaying) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val image = track.albumCoverUri

            if (image != null) {
                AsyncImage(
                    model = track.albumCoverUri,
                    contentDescription = stringResource(R.string.album_cover),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.default_album_cover),
                    contentDescription = stringResource(R.string.album_cover),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            val mod = if (isPlaying || isCurrentTrack) Modifier.widthIn(0.dp, 250.dp) else Modifier
            Column {
                Text(
                    text = track.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = mod
                )
                Text(
                    text = track.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                    modifier = mod
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isPlaying) {
                IconButton(
                    onClick = {
                        onPlayPauseClick()
                    }
                ) {
                    val icon = if (isPause) painterResource(R.drawable.play) else painterResource(R.drawable.pause)
                    Icon(
                        painter = icon,
                        contentDescription = stringResource(R.string.current_track)
                    )
                }
            } else if (isCurrentTrack) {
                Icon(
                    painter = painterResource(R.drawable.playing),
                    contentDescription = stringResource(R.string.current_track)
                )
            }
        }
    }
}