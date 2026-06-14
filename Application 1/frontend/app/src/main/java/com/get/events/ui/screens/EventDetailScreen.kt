package com.get.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.data.model.Event
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.get.events.data.local.TokenDataStore
import com.get.events.navigation.Routes
import com.get.events.viewmodel.EventsViewModel
import com.get.events.ui.theme.*

@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit = {},
    onBuyTicket: () -> Unit = {},
    onContestRegister: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    vm: EventsViewModel = viewModel()
) {
    val context = LocalContext.current
    var isVisitor by remember { mutableStateOf(true) }
    var showLoginDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        TokenDataStore.isVisitor(context).collect { isVisitor = it }
    }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Inscription requise") },
            text = { Text("Pour acheter un ticket, vous devez créer un compte ou vous connecter.") },
            confirmButton = {
                TextButton(onClick = {
                    showLoginDialog = false
                    onNavigateToLogin()
                }) { Text("S'inscrire / Se connecter") }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) { Text("Annuler") }
            }
        )
    }

    val selected by vm.selectedEvent.collectAsState()

    LaunchedEffect(eventId) { vm.loadEvent(eventId) }

    val event = selected
    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenDark)
        }
        return
    }

    val hasContest = event.category.equals("CONCOURS", ignoreCase = true)
    var isSaved by remember { mutableStateOf(event.isSaved) }
    val scrollState = rememberScrollState()

    val occupancyFraction = event.reservedPlaces.toFloat() / event.totalPlaces.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
    ) {
        // ── Hero image ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(event.backgroundColor))
        ) {
            // Emoji illustration
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = event.categoryEmoji, fontSize = 80.sp)
            }

            // Back button
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite.copy(alpha = 0.9f))
                    .clickable { onBack() }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Share button
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite.copy(alpha = 0.9f))
                    .clickable { /* share */ }
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Partager",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Content card ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundLight)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Category badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(GreenSurface)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${event.categoryEmoji} ${event.category}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // Title
            Text(
                text = event.title,
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(Modifier.height(16.dp))

            // Info card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceWhite)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EventInfoRow(
                    icon = Icons.Default.CalendarToday,
                    text = event.dateLabel
                )
                EventInfoRow(
                    icon = Icons.Default.LocationOn,
                    text = event.location
                )
                EventInfoRow(
                    icon = Icons.Default.Group,
                    text = "Places limitées — ${event.reservedPlaces} / ${event.totalPlaces} réservées"
                )

                // Progress bar
                LinearProgressIndicator(
                    progress = occupancyFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = GreenMedium,
                    trackColor = GreenSurface
                )
            }

            Spacer(Modifier.height(16.dp))

            // Description
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(Modifier.height(32.dp))

            // ── Action buttons ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Save button
                OutlinedButton(
                    onClick = {
                        isSaved = !isSaved
                        // TODO: viewModel.toggleSave(event.id, isSaved)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = GreenSurface,
                        contentColor = GreenDark
                    ),
                    border = null
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isSaved) "Sauvegardé" else "Sauvegarder",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Buy ticket or Contest register
                Button(
                    onClick = {
                        if (isVisitor && !hasContest) {
                            showLoginDialog = true
                        } else if (hasContest) {
                            onContestRegister(eventId)
                        } else {
                            onBuyTicket()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenDark,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (hasContest) Icons.Default.EmojiEvents else Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (hasContest) "S'inscrire" else "Acheter\nun ticket",
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EventInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GreenMedium,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
        )
    }
}
