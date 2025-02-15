package com.yanetto.local_tracks.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val LOCAL_TRACKS_ROUTE = "local_tracks_screen"

fun NavGraphBuilder.localTracksScreen(
    navigateToPlayer: () -> Unit
) {
    composable(
        route = LOCAL_TRACKS_ROUTE,
        enterTransition = { fadeIn(animationSpec = tween(durationMillis = 1200)) },
        exitTransition = { fadeOut(animationSpec = tween(durationMillis = 800)) }
    ) {
        var permissionGranted by remember { mutableStateOf(false) }

        RequestStoragePermission(onPermissionGranted = {
            permissionGranted = true
        }) {
            if (permissionGranted) {
                LocalTracksScreen(navigateToPlayer = navigateToPlayer)
            }
        }
    }
}

@Composable
fun RequestStoragePermission(
    onPermissionGranted: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionGranted = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted.value = isGranted
        if (isGranted) onPermissionGranted()
    }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission)
        } else {
            permissionGranted.value = true
            onPermissionGranted()
        }
    }

    if (permissionGranted.value) content()
}