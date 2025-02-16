package com.yanetto.musicplayerapp.presentation.components

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.layout.padding
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigation
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigationItem
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yanetto.musicplayer.ui.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {
    val screens = listOf(
        Screen.LocalTracks,
        Screen.RemoteTracks
    )

    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        screens.forEach { screen ->
            val icon = screen.imageResId
            BottomNavigationItem(
                icon = {
                    if (icon != null) Icon(
                        painterResource(id = screen.imageResId),
                        contentDescription = stringResource(screen.titleResId),
                        tint = if (currentRoute == screen.route) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground.copy(0.5f)
                    )
                },
                label = {
                    Text(
                        text = stringResource(screen.titleResId),
                        color = if (currentRoute == screen.route) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground.copy(0.5f)
                    )
                },
                selected = currentRoute == screen.route,
                onClick = {

                    if (currentRoute != screen.route) {
                        navController.popBackStack(Screen.LocalTracks.route, inclusive = false)
                        navController.popBackStack(Screen.RemoteTracks.route, inclusive = false)

                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}