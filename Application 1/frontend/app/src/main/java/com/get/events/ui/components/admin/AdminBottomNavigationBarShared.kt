package com.get.events.ui.components.admin

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.navigation.admin.AdminRoutes
import com.get.events.ui.theme.*

private data class AdminNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val adminNavItems = listOf(
    AdminNavItem(AdminRoutes.DASHBOARD,  "Dashboard",  Icons.Default.Dashboard),
    AdminNavItem(AdminRoutes.EVENEMENTS, "Événements", Icons.Default.CalendarMonth),
    AdminNavItem(AdminRoutes.PAIEMENTS, "Paiements",  Icons.Default.Payment),
    AdminNavItem(AdminRoutes.INSCRITS,   "Inscrits",   Icons.Default.People),
    AdminNavItem(AdminRoutes.CONCOURS,   "Concours",   Icons.Default.EmojiEvents),
)

@Composable
fun AdminBottomNavigationBarShared(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = BackgroundWhite,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets,
        modifier = Modifier.fillMaxWidth()
    ) {
        adminNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label, fontSize = 11.sp)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenDark,
                    selectedTextColor = GreenDark,
                    indicatorColor = GreenDark.copy(alpha = 0.12f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}
