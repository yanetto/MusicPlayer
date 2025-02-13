package com.yanetto.common_tracks.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yanetto.common_tracks.R

@Composable
fun TracksScreen(
    modifier: Modifier = Modifier,
    uiState: TracksUiState,
    onSearchTracks: (String) -> Unit,
    onLoadTracks: () -> Unit,
    onTrackClick: (Int) -> Unit,
    navigateToPlayer: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

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
                val tracks = uiState.tracks
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(tracks) { track ->
                            TrackItem(track = track, isPlaying = false) { onTrackClick(tracks.indexOf(track)) }
                        }
                    }
                    if ((uiState as? TracksUiState.Success)?.currentTrack != null) {
                        val track = (uiState as? TracksUiState.Success)?.currentTrack
                        TrackItem(
                            isPlaying = true,
                            track = track!!,
                            onItemClick = { navigateToPlayer() }
                        )
                    }
                }
            }
            is TracksUiState.Error -> {
                val message = uiState.message
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = message ?: stringResource(R.string.unknown_error_occurred))
                }
            }
            is TracksUiState.Empty -> {
                EmptyState()
            }
        }

        if ((uiState as? TracksUiState.Success)?.currentTrack != null) {
            val track = (uiState as? TracksUiState.Success)?.currentTrack
            TrackItem(
                isPlaying = true,
                track = track!!,
                onItemClick = { navigateToPlayer() }
            )
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
    isPlaying: Boolean,
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
            modifier = Modifier.padding(16.dp)
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
            Column {
                Text(
                    text = track.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isPlaying) {
                IconButton(onClick = {}){
                    Icon(painter = painterResource(R.drawable.playing), contentDescription = stringResource(R.string.current_track))
                }
            }
        }
    }
}