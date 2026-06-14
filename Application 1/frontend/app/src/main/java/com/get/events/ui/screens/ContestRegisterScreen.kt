package com.get.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.get.events.data.api.RetrofitClient
import com.get.events.data.repository.FakeData
import com.get.events.ui.theme.*
import com.get.events.viewmodel.OrdersViewModel

@Composable
fun ContestRegisterScreen(
    eventId: String,
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {},
    ordersVm: OrdersViewModel = viewModel()
) {
    val contest = FakeData.contests.find { it.eventId == eventId }
        ?: FakeData.contests.first()

    var teamsCount by remember { mutableStateOf(0) }

    LaunchedEffect(eventId) {
        val id = eventId.toIntOrNull()
        if (id != null) {
            try {
                teamsCount = RetrofitClient.instance.getContestTeamCount(id).teamsCount
            } catch (_: Exception) {
                teamsCount = 0
            }
        }
    }

    var teamName by remember { mutableStateOf("") }
    var projectTheme by remember { mutableStateOf("") }
    var membersCount by remember { mutableStateOf("3") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retour",
                tint = TextPrimary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable { onBack() }
            )
            Text(
                text = "S'inscrire au\nconcours",
                style = MaterialTheme.typography.displayMedium.copy(
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            // ── Contest recap card ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GreenSurface)
                    .padding(20.dp)
            ) {
                Text(text = "🏆", fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = contest.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${contest.dateLabel} · ${contest.reservedForLabel}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // "Ouvert" badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(GreenMint)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "📌 Ouvert",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    // Teams count badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFEEEEEE))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$teamsCount équipe${if (teamsCount != 1) "s" else ""} inscrite${if (teamsCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Formulaire ────────────────────────────────────────────────

            // Nom de l'équipe
            ContestTextField(
                label = "Nom de l'équipe",
                value = teamName,
                onValueChange = { teamName = it },
                placeholder = "Ex : Team Innovateurs",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            Spacer(Modifier.height(16.dp))

            // Thème du projet
            ContestTextField(
                label = "Thème du projet",
                value = projectTheme,
                onValueChange = { projectTheme = it },
                placeholder = "Ex : IoT, 5G, Signal..."
            )

            Spacer(Modifier.height(16.dp))

            // Nombre de membres
            ContestTextField(
                label = "Nombre de membres",
                value = membersCount,
                onValueChange = { membersCount = it },
                placeholder = "Ex : 3",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = if (membersCount.isNotEmpty()) " membres" else ""
            )

            Spacer(Modifier.height(24.dp))

            // Disclaimer
            Text(
                text = "En confirmant, vous acceptez les règles du concours GET 2025.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Submit button ─────────────────────────────────────────────
            Button(
                onClick = {
                    if (teamName.isNotBlank() && projectTheme.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        val members = membersCount.toIntOrNull() ?: 1
                        ordersVm.createContestRegistration(
                            eventId = eventId,
                            teamName = teamName.trim(),
                            projectTheme = projectTheme.trim(),
                            membersCount = members,
                            onSuccess = {
                                isLoading = false
                                onSuccess()
                            },
                            onError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenDark,
                    contentColor = Color.White
                ),
                enabled = teamName.isNotBlank() && projectTheme.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Confirmer l'inscription",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ContestTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    suffix: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEEEEE))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
                keyboardOptions = keyboardOptions,
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.titleMedium.copy(color = TextHint)
                        )
                    }
                    inner()
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            if (suffix.isNotEmpty() && value.isNotEmpty()) {
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                )
            }
        }
    }
}
