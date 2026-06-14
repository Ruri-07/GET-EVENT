package com.get.events.ui.screens.admin.concours

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.get.events.data.api.ContestRegistrationDto
import com.get.events.ui.theme.*
import com.get.events.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidateConcoursScreen(
    registration: ContestRegistrationDto,
    onBack: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    vm: AdminViewModel
) {
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Valider l'inscription") },
            text = { Text("Confirmer l'équipe « ${registration.teamName} » pour le concours ?") },
            confirmButton = {
                TextButton(onClick = {
                    showApproveDialog = false
                    vm.approveContestRegistration(registration.id, onDone = onApprove)
                }) { Text("Valider") }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Refuser l'inscription") },
            text = { Text("Refuser l'équipe « ${registration.teamName} » ?") },
            confirmButton = {
                TextButton(onClick = {
                    showRejectDialog = false
                    vm.rejectContestRegistration(registration.id, onDone = onReject)
                }) { Text("Refuser", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vérifier inscription concours", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Équipe", fontWeight = FontWeight.Bold, color = GreenDark)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Nom de l'équipe", registration.teamName)
                    InfoRow("Thème du projet", registration.projectTheme)
                    InfoRow("Nombre de membres", registration.membersCount.toString())
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Participant", fontWeight = FontWeight.Bold, color = GreenDark)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Nom", registration.userName ?: "—")
                    InfoRow("Email", registration.userEmail ?: "—")
                    InfoRow("Événement", registration.eventTitle ?: "Concours Mini-Projet")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Refuser", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { showApproveDialog = true },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Valider", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
