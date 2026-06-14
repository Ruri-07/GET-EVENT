package com.get.events

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.get.events.data.EventsRefreshNotifier
import com.get.events.navigation.GetEventsNavGraph
import com.get.events.navigation.Screen
import com.get.events.navigation.bottomBarRoutes
import com.get.events.ui.components.GetEventsBottomBar
import com.get.events.data.api.RetrofitClient
import com.get.events.data.local.TokenDataStore
import com.get.events.ui.theme.GetEventsTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            RetrofitClient.authToken = TokenDataStore.getToken(this@MainActivity).first()
        }
        enableEdgeToEdge()
        setContent {
            GetEventsTheme {
                val navController      = rememberNavController()
                val navBackStackEntry  by navController.currentBackStackEntryAsState()
                val currentRoute       = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in bottomBarRoutes

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    ),
                    bottomBar = {
                        if (showBottomBar) {
                            Surface(
                                shadowElevation = 8.dp,
                                tonalElevation = 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                GetEventsBottomBar(
                                    currentRoute   = currentRoute,
                                    onNavItemClick = { route ->
                                        if (route == Screen.Home.route || route == Screen.Events.route) {
                                            EventsRefreshNotifier.notifyRefresh()
                                        }
                                        navController.navigate(route) {
                                            popUpTo(com.get.events.navigation.Screen.Home.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState    = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    GetEventsNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
