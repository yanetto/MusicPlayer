package com.yanetto.musicplayer.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yanetto.local_tracks.presentation.localTracksScreen
import com.yanetto.music_player.presentation.musicPlayerScreen
import com.yanetto.music_player.presentation.navigateToMusicPlayer
import com.yanetto.musicplayerapp.presentation.components.BottomNavigationBar
import com.yanetto.remote_tracks.presentation.remoteTracksScreen


@Composable
fun MusicPlayerApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var currentRoute by remember { mutableStateOf("") }

    LaunchedEffect(navBackStackEntry) {
        currentRoute = navBackStackEntry?.destination?.route.toString()
    }

    val context = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        context.intent?.data?.let { uri ->
            if (uri.host == "player") {
                navController.navigate("player_screen")
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute != Screen.PlayerScreen.route) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            startDestination = Screen.LocalTracks.route
        ) {
            localTracksScreen(navigateToPlayer = { navController.navigateToMusicPlayer() })

            remoteTracksScreen(navigateToPlayer = { navController.navigateToMusicPlayer() })

            musicPlayerScreen(navigateBack = { navController.popBackStack() })
        }
    }
}