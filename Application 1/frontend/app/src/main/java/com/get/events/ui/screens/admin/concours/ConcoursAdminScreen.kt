package com.get.events.ui.screens.admin.concours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.get.events.data.api.ContestRegistrationDto
import com.get.events.ui.components.admin.AdminBottomNavigationBarShared
import com.get.events.ui.theme.*
import com.get.events.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcoursAdminScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onRegistrationClick: (String) -> Unit,
    vm: AdminViewModel
) {
    val registrations by vm.contestRegistrations.collectAsState()
    val pending = registrations.filter { it.status.equals("PENDING", ignoreCase = true) }

    LaunchedEffect(Unit) { vm.loadContestRegistrations() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Concours Mini-Projet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        bottomBar = {
            Surface(shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                AdminBottomNavigationBarShared(currentRoute = currentRoute, onNavigate = onNavigate)
            }
        },
        containerColor = BackgroundWhite
    ) { padding ->
        if (pending.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucune inscription concours en attente", color = TextHint)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pending) { reg ->
                    ConcoursCard(registration = reg, onClick = { onRegistrationClick(reg.id.toString()) })
                }
            }
        }
    }
}

@Composable
private fun ConcoursCard(registration: ContestRegistrationDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(registration.teamName, fontWeight = FontWeight.Bold)
                Text(
                    registration.userName ?: registration.userEmail ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${registration.eventTitle ?: "Concours"} · Thème: ${registration.projectTheme}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${registration.membersCount} membre(s) · En attente",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextHint)
        }
    }
}
