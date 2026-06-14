package com.get.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.get.events.navigation.Screen
import com.get.events.ui.theme.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Accueil", Icons.Filled.Home, Screen.Home.route),
    BottomNavItem("Événements", Icons.Filled.Event, Screen.Events.route),
    BottomNavItem("Mes Tickets", Icons.Filled.ConfirmationNumber, Screen.MyTickets.route),
    BottomNavItem("Profil", Icons.Filled.Person, Screen.Profile.route)
)

@Composable
fun GetEventsBottomBar(
    currentRoute: String?,
    onNavItemClick: (String) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceWhite,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavItemClick(item.route) },
                icon = {
                    Box(
                        modifier = if (selected) Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(NavSelected)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                        else Modifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) GreenDark else NavUnselected
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) GreenDark else NavUnselected
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
