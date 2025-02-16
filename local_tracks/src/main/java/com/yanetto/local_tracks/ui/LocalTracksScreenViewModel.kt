package com.yanetto.local_tracks.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.yanetto.common_tracks.presentation.TracksScreenViewModel
import com.yanetto.local_tracks.data.repository.MediaStoreTracksRepository
import com.yanetto.music_player.domain.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PermissionUiState(
    val permissionGranted: Boolean = false,
    val permissionChecked: Boolean = false
)

@HiltViewModel
internal class LocalTracksScreenViewModel @Inject constructor(
    repository: MediaStoreTracksRepository,
    musicPlayer: MusicPlayerController
) : TracksScreenViewModel(repository, musicPlayer) {
    private val _permissionState = MutableStateFlow(PermissionUiState())
    val permissionState: StateFlow<PermissionUiState> = _permissionState

    private val permission: String =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun checkPermission(context: Context) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
        _permissionState.update {
            PermissionUiState(
                permissionGranted = granted,
                permissionChecked = true
            )
        }
        if (granted) notStarted()
    }

    fun onPermissionResult(isGranted: Boolean) {
        _permissionState.update {
            _permissionState.value.copy(
                permissionGranted = isGranted,
                permissionChecked = true
            )
        }
        if (isGranted) notStarted()
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

