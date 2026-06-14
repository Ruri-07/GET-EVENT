package com.getticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.getticket.data.api.RepVerification
import com.getticket.ui.theme.BadgeRed
import com.getticket.ui.theme.GreenDark

@Composable
fun EcranResultatRefuse(
    resultat: RepVerification,
    onScannerAutre: () -> Unit,
    onSignalerProbleme: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().background(colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onScannerAutre) {
                Icon(Icons.Default.ArrowBack, "Retour", tint = GreenDark)
            }
            Text(
                "Résultat",
                color = GreenDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }

        Box(
            modifier = Modifier.fillMaxWidth().background(BadgeRed).padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFB91C1C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Ticket Invalide",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text("Accès refusé", color = Color(0xFFFFCDD2), fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(colors.surface).padding(16.dp)
            ) {
                Text(
                    "Raison du refus :",
                    color = colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFFFEBEE)).padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(BadgeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("!", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        resultat.raisonRefus.ifEmpty { "Ticket non valide" },
                        color = BadgeRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text("Autres raisons possibles :", color = colors.onSurfaceVariant, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))

                listOf(
                    "QR code expiré ou falsifié",
                    "Paiement non validé dans GET Events",
                    "Ticket annulé par l'administrateur"
                ).forEach { raison ->
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text("• $raison", color = colors.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }

            BoutonScannerAutre(onScannerAutre)

            OutlinedButton(
                onClick = onSignalerProbleme,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurfaceVariant)
            ) {
                Text("Signaler un problème", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
