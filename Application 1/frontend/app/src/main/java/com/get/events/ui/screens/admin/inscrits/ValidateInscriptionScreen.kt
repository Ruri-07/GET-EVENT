package com.get.events.ui.screens.admin.inscrits

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.get.events.data.api.RetrofitClient
import com.get.events.data.api.UserDto
import com.get.events.ui.theme.*
import com.get.events.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidateInscriptionScreen(
    user: UserDto,
    onBack: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    vm: AdminViewModel
) {
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    val cardUrl = RetrofitClient.mediaUrl(user.studentCardUrl)

    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Valider l'inscription") },
            text = { Text("Confirmer l'inscription de ${user.fullName} ?") },
            confirmButton = {
                TextButton(onClick = {
                    showApproveDialog = false
                    vm.approveUser(user.id, onDone = onApprove)
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
            text = { Text("Refuser l'inscription de ${user.fullName} ? L'utilisateur ne pourra pas se connecter.") },
            confirmButton = {
                TextButton(onClick = {
                    showRejectDialog = false
                    vm.rejectUser(user.id, onDone = onReject)
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
                title = { Text("Vérifier inscription", fontWeight = FontWeight.Bold) },
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
                    Text("Informations personnelles", fontWeight = FontWeight.Bold, color = GreenDark)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Nom complet", user.fullName)
                    InfoRow("Email", user.email)
                    InfoRow("Téléphone", user.phone?.ifBlank { null } ?: "—")
                    InfoRow("Type de compte", user.userType)
                    InfoRow("Mention", user.mention)
                    if (user.userType != "Enseignant") {
                        InfoRow("Année / Niveau", user.year.ifBlank { "—" })
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (user.userType == "Enseignant") {
                        Text("Photo CIN", fontWeight = FontWeight.Bold, color = GreenDark)
                        Spacer(Modifier.height(12.dp))
                        if (cardUrl != null) {
                            AsyncImage(
                                model = cardUrl,
                                contentDescription = "Photo CIN",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BackgroundLight),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BackgroundLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aucune photo CIN fournie", color = TextHint)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Vérifiez que la photo CIN correspond à l'enseignant inscrit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        Text("Carte étudiant", fontWeight = FontWeight.Bold, color = GreenDark)
                        Spacer(Modifier.height(12.dp))
                        if (cardUrl != null) {
                            AsyncImage(
                                model = cardUrl,
                                contentDescription = "Carte étudiant",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BackgroundLight),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BackgroundLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aucune photo fournie", color = TextHint)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Vérifiez que la carte correspond à l'étudiant inscrit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
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
