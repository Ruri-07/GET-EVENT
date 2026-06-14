package com.get.events.ui.screens.admin.inscrits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
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
import com.get.events.ui.components.admin.AdminBottomNavigationBarShared
import com.get.events.ui.theme.*
import com.get.events.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InscritsAdminScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    vm: AdminViewModel
) {
    val users by vm.users.collectAsState()
    val pendingUsers = users.filter {
        it.role != TokenDataStore.ROLE_ADMIN && it.registrationStatus == "PENDING"
    }

    LaunchedEffect(Unit) { vm.loadUsers() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inscriptions à valider", fontWeight = FontWeight.Bold) },
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
        if (pendingUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucune inscription en attente", color = TextHint)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pendingUsers) { user ->
                    InscritCard(user = user, onClick = { onUserClick(user.id.toString()) })
                }
            }
        }
    }
}

@Composable
private fun InscritCard(user: UserDto, onClick: () -> Unit) {
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
                Text(user.fullName, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                user.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    Text(phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (user.userType == "Enseignant") {
                        "${user.userType} · Photo CIN fournie · ${user.mention}"
                    } else {
                        "${user.userType} · ${user.year} · ${user.mention}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "En attente de vérification",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextHint)
        }
    }
}
