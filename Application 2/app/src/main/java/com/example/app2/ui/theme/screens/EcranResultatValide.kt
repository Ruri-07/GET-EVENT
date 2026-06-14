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
import com.getticket.ui.theme.GreenDark
import com.getticket.ui.theme.GreenLight
import com.getticket.ui.theme.GreenMedium
import com.getticket.ui.theme.GreenMint

@Composable
fun EcranResultatValide(
    resultat: RepVerification,
    onScannerAutre: () -> Unit
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
            modifier = Modifier.fillMaxWidth().background(GreenMedium).padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(GreenDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = GreenLight, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Ticket Valide",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text("L'accès est autorisé", color = GreenLight, fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(colors.surface).padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) { Text("👤", fontSize = 22.sp) }
                    Column {
                        Text(
                            resultat.nomClient,
                            color = colors.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        val sub = listOf(resultat.categorie, resultat.promotion)
                            .filter { it.isNotEmpty() }
                            .joinToString(" · ")
                        Text(sub, color = colors.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = colors.outline)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoCard("Ticket", resultat.codeTicket, Modifier.weight(1f))
                InfoCard("Type", resultat.typeTicket, Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(GreenMint).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("✅", fontSize = 18.sp)
                Text(
                    if (resultat.premiereUtilisation) "Première utilisation · Non utilisé"
                    else "Utilisation confirmée",
                    color = GreenDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))
            BoutonScannerAutre(onScannerAutre)
        }
    }
}

@Composable
fun InfoCard(label: String, valeur: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .background(colors.surface).padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(label, color = colors.onSurfaceVariant, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(valeur, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun BoutonScannerAutre(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
    ) {
        Text("Scanner un autre ticket", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
