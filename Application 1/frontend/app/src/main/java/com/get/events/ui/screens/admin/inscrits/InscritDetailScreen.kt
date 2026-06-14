package com.get.events.ui.screens.admin.inscrits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InscritDetailScreen(
    user: UserDto,
    onBack: () -> Unit
) {
    val cardUrl = RetrofitClient.mediaUrl(user.studentCardUrl)
    val statusLabel = when (user.registrationStatus) {
        "APPROVED" -> "Validé"
        "REJECTED" -> "Refusé"
        else       -> "En attente de validation"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail inscrit", fontWeight = FontWeight.Bold) },
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
                    DetailRow("Nom complet", user.fullName)
                    DetailRow("Email", user.email)
                    DetailRow("Téléphone", user.phone?.ifBlank { null } ?: "—")
                    DetailRow("Type de compte", user.userType)
                    DetailRow("Mention", user.mention)
                    if (user.userType != "Enseignant") {
                        DetailRow("Année / Niveau", user.year.ifBlank { "—" })
                    }
                    DetailRow("Statut", statusLabel)
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
                    val docTitle = if (user.userType == "Enseignant") "Photo CIN" else "Carte étudiant"
                    Text(docTitle, fontWeight = FontWeight.Bold, color = GreenDark)
                    Spacer(Modifier.height(12.dp))
                    if (cardUrl != null) {
                        AsyncImage(
                            model = cardUrl,
                            contentDescription = docTitle,
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
                            Text("Aucun document fourni", color = TextHint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
