package com.yanetto.local_tracks.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanetto.common_tracks.presentation.TracksScreen
import com.yanetto.common_tracks.presentation.TracksUiState
import com.yanetto.local_tracks.R

@Composable
internal fun LocalTracksScreenCheckPermission(
    viewModel: LocalTracksScreenViewModel = hiltViewModel(),
    navigateToPlayer: () -> Unit
) {
    val permissionUiState by viewModel.permissionState.collectAsState()

    RequestStoragePermission(
        onPermissionResult = viewModel::onPermissionResult
    )

    if (permissionUiState.permissionGranted) {
        LocalTracksScreen(navigateToPlayer = navigateToPlayer, viewModel = viewModel)
    } else {
        NoPermissionScreen({ viewModel.openAppSettings(it) }, { viewModel.checkPermission(it) })
    }
}

@Composable
fun RequestStoragePermission(
    onPermissionResult: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(permission)
        }
    }
}


@Composable
fun NoPermissionScreen(
    onOpenSettings: (Context) -> Unit,
    checkPermission: (Context) -> Unit
) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.ask_permission_mediafiles),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = {
                checkPermission(context)
            }) {
                Text(stringResource(R.string.i_gave_permission))
            }
            TextButton(onClick = {
                onOpenSettings(context)
            }) {
                Text(stringResource(R.string.go_to_settings))
            }
        }
    }
}

@Composable
internal fun LocalTracksScreen(
    modifier: Modifier = Modifier,
    navigateToPlayer: () -> Unit,
    viewModel: LocalTracksScreenViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is TracksUiState.NotStarted) {
            viewModel.loadTracks()
        }
    }

    TracksScreen(
        modifier = modifier,
        uiState = uiState,
        onSearchTracks = { viewModel.searchTracks(it) },
        onLoadTracks = { viewModel.loadTracks() },
        onTrackClick = {
            viewModel.setAndPlayPlaylist(
                (uiState as? TracksUiState.Success)?.tracks ?: emptyList(),
                it
            )
        },
        isCurrentPlayingTrack = { viewModel.isCurrentPlayingTrack(it) },
        navigateToPlayer = navigateToPlayer,
        onPlayPauseClick = { viewModel.changePlayingState() },
        loadNext = { viewModel.loadNext() }
    )
}