package com.get.events.ui.screens.admin.inscrits

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
import com.get.events.data.api.UserDto
import com.get.events.data.local.TokenDataStore
import com.get.events.ui.theme.*
import com.get.events.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InscritsOverviewScreen(
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    vm: AdminViewModel
) {
    val users by vm.users.collectAsState()
    val registeredUsers = users.filter { it.role != TokenDataStore.ROLE_ADMIN }

    LaunchedEffect(Unit) { vm.loadUsers() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liste des inscrits", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (registeredUsers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun inscrit pour le moment", color = TextHint)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "${registeredUsers.size} inscrit(s) — étudiants et enseignants",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(registeredUsers) { user ->
                    InscritOverviewCard(user = user, onClick = { onUserClick(user.id.toString()) })
                }
            }
        }
    }
}

@Composable
private fun InscritOverviewCard(user: UserDto, onClick: () -> Unit) {
    val statusLabel = when (user.registrationStatus) {
        "APPROVED" -> "Validé"
        "REJECTED" -> "Refusé"
        else       -> "En attente"
    }
    val statusColor = when (user.registrationStatus) {
        "APPROVED" -> GreenDark
        "REJECTED" -> MaterialTheme.colorScheme.error
        else       -> GoldPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                user.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    Text(phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (user.userType == "Enseignant") {
                        "${user.userType} · ${user.mention}"
                    } else {
                        "${user.userType} · ${user.year} · ${user.mention}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = statusLabel,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextHint)
        }
    }
}
