package com.get.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.ui.theme.*

// ─── Data class pour l'écran ─────────────────────────────────────────────────
// Ces données viendront du back end (réponse après soumission du paiement)
data class PendingTicketInfo(
    val eventOrganizer: String,       // ex : "GET · Télécommunications"
    val eventTitle: String,           // ex : "Grande Réception 2025"
    val eventDateLabel: String,       // ex : "14 Juin 2025 · 19h00"
    val isContest: Boolean = false,
    val amountLabel: String? = null
)

// ─── PaymentPendingScreen ─────────────────────────────────────────────────────
/**
 * Écran affiché juste après la soumission d'un paiement Mobile Money.
 * Le ticket est en attente de validation par l'administrateur.
 *
 * [ticketInfo]   – informations du ticket retournées par l'API après paiement
 * [onBack]       – navigation vers Mes Tickets (popBackStack ou navigate)
 */
@Composable
fun PaymentPendingScreen(
    ticketInfo: PendingTicketInfo,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Mes Tickets",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ── Contenu scrollable ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Icône sablier
            Text(text = "⏳", fontSize = 72.sp)

            Spacer(Modifier.height(20.dp))

            // Titre principal
            Text(
                text = if (ticketInfo.isContest) "Inscription soumise !" else "Paiement soumis !",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // Sous-titre descriptif
            Text(
                text = if (ticketInfo.isContest) {
                    "Votre inscription au concours Mini-Projet est en cours de\nvérification par l'administrateur. Aucun paiement n'est requis."
                } else {
                    "Votre paiement est en cours de vérification par\nl'administrateur. Vous recevrez votre ticket QR code\nune fois validé."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // ── Carte ticket en attente ───────────────────────────────────
            PendingTicketCard(ticketInfo = ticketInfo)

            Spacer(Modifier.height(20.dp))

            // ── Bandeau info délai ────────────────────────────────────────
            InfoBanner(
                text = "La validation prend généralement 24h ouvrées. Vous serez notifié par notification push."
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Carte ticket en attente ──────────────────────────────────────────────────
@Composable
private fun PendingTicketCard(ticketInfo: PendingTicketInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Partie haute : fond vert foncé
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenDark)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Infos événement
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ticketInfo.eventOrganizer,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GreenLight,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = ticketInfo.eventTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📅", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = ticketInfo.eventDateLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GreenLight
                            )
                        )
                    }
                    ticketInfo.amountLabel?.let { amount ->
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💰", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = amount,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = GreenLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Badge "EN ATTENTE"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF8B6914))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏳", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "EN\nATTENTE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = YellowBadge,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }

        // Séparateur dentelé (effet ticket)
        TicketDivider()

        // Partie basse : fond blanc — zone QR code
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "QR Code disponible après validation",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Séparateur dentelé style ticket ─────────────────────────────────────────
@Composable
private fun TicketDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(SurfaceWhite)
    ) {
        // Demi-cercle gauche
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = (-12).dp)
                .clip(RoundedCornerShape(50))
                .background(BackgroundLight)
                .align(Alignment.CenterStart)
        )
        // Ligne tiretée centrale
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(1.dp)
                .align(Alignment.Center)
                .border(
                    width = 1.dp,
                    color = DividerColor,
                    shape = RoundedCornerShape(0.dp)
                )
        )
        // Demi-cercle droit
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = 12.dp)
                .clip(RoundedCornerShape(50))
                .background(BackgroundLight)
                .align(Alignment.CenterEnd)
        )
    }
}

// ─── Bandeau info ─────────────────────────────────────────────────────────────
@Composable
private fun InfoBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GreenSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = GreenMedium,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 1.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = GreenMedium,
                lineHeight = 20.sp
            )
        )
    }
}
