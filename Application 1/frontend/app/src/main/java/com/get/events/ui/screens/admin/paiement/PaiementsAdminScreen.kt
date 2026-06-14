package com.get.events.ui.screens.admin.paiement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.get.events.data.model.admin.PaiementEnAttente
import com.get.events.ui.components.admin.AdminBottomNavigationBarShared
import com.get.events.ui.components.admin.PaiementItem
import com.get.events.ui.theme.*
import com.get.events.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaiementsAdminScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onPaiementClick: (String) -> Unit,
    vm: AdminViewModel
) {
    val dashboardState by vm.dashboardState.collectAsState()

    LaunchedEffect(Unit) { vm.loadDashboard() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paiements", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val paiements = dashboardState.paiementsEnAttente
            if (paiements.isEmpty()) {
                item {
                    Text(
                        "Aucun paiement en attente",
                        color = TextHint,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                items(paiements) { paiement ->
                    PaiementListCard(paiement = paiement, onClick = { onPaiementClick(paiement.id) })
                }
            }
        }
    }
}

@Composable
private fun PaiementListCard(paiement: PaiementEnAttente, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        PaiementItem(
            paiement = paiement,
            showDivider = false,
            onClick = onClick
        )
    }
}
