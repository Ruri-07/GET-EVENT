package com.getticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.getticket.data.api.ApiClient
import com.getticket.data.api.LigneScan
import com.getticket.data.api.RepStatsHistorique
import com.getticket.ui.theme.GreenDark
import com.getticket.ui.theme.GreenMedium
import com.getticket.ui.theme.GreenMint
import com.getticket.ui.theme.GreenSurface
import com.getticket.ui.theme.YellowBadge

@Composable
fun EcranHistorique(
    token: String,
    onRetour: () -> Unit
) {
    val api    = remember { ApiClient() }
    val colors = MaterialTheme.colorScheme
    var recherche  by remember { mutableStateOf("") }
    var lignes     by remember { mutableStateOf<List<LigneScan>>(emptyList()) }
    var stats      by remember { mutableStateOf<RepStatsHistorique?>(null) }
    var chargement by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val rep = api.chargerHistorique(token)
        lignes     = rep?.lignes ?: emptyList()
        stats      = rep?.stats
        chargement = false
    }

    LaunchedEffect(recherche) {
        val rep = api.chargerHistorique(token, recherche)
        lignes = rep?.lignes ?: emptyList()
        stats  = rep?.stats
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRetour) {
                Icon(Icons.Default.ArrowBack, "Retour", tint = GreenDark)
            }
            Text(
                "Historique des\nscans",
                color = GreenDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(48.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(28.dp))
                    .background(colors.surface).padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🔍", fontSize = 15.sp)
                BasicTextField(
                    value = recherche,
                    onValueChange = { recherche = it },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.onSurface, fontSize = 14.sp),
                    cursorBrush = SolidColor(GreenMedium),
                    decorationBox = { inner ->
                        if (recherche.isEmpty()) {
                            Text("Rechercher...", color = colors.onSurfaceVariant, fontSize = 14.sp)
                        }
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        when {
            chargement -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenMedium)
            }
            lignes.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Aucun scan trouvé", color = colors.onSurfaceVariant, fontSize = 14.sp)
            }
            else -> LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().background(colors.surface)
            ) {
                items(lignes) { ligne ->
                    LigneHistoriqueItem(ligne)
                    HorizontalDivider(color = colors.outline, thickness = 1.dp)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().background(colors.surface)) {
            StatItemHisto(stats?.valides ?: 0, "Validés", GreenMedium, Modifier.weight(1f), colors)
            HorizontalDivider(
                modifier = Modifier.width(1.dp).height(64.dp).align(Alignment.CenterVertically),
                color = colors.outline
            )
            StatItemHisto(stats?.refuses ?: 0, "Refusés", colors.error, Modifier.weight(1f), colors)
            HorizontalDivider(
                modifier = Modifier.width(1.dp).height(64.dp).align(Alignment.CenterVertically),
                color = colors.outline
            )
            StatItemHisto(stats?.doublons ?: 0, "Doublons", YellowBadge, Modifier.weight(1f), colors)
        }
    }
}

@Composable
fun LigneHistoriqueItem(ligne: LigneScan) {
    val colors = MaterialTheme.colorScheme
    val (couleurAv, icone, couleurSub) = when (ligne.statut) {
        "valide"  -> Triple(GreenSurface, "✓", colors.onSurfaceVariant)
        "refuse"  -> Triple(Color(0xFFFFEBEE), "✕", colors.error)
        "doublon" -> Triple(Color(0xFFFFF3E0), "⚠", YellowBadge)
        else      -> Triple(colors.surfaceVariant, "?", colors.onSurfaceVariant)
    }
    val couleurIcone = when (ligne.statut) {
        "valide"  -> GreenMedium
        "refuse"  -> colors.error
        "doublon" -> YellowBadge
        else      -> colors.onSurfaceVariant
    }
    val (bgBadge, txtBadge, labelBadge) = when (ligne.statut) {
        "valide"  -> Triple(GreenMint, GreenDark, "✓ Valide")
        "refuse"  -> Triple(Color(0xFFFFEBEE), colors.error, "✕ Refusé")
        "doublon" -> Triple(Color(0xFFFFF3E0), YellowBadge, "⚠ Doublé")
        else      -> Triple(colors.surfaceVariant, colors.onSurfaceVariant, "?")
    }

    val heure = if (ligne.heureUtilisation.length >= 16)
        ligne.heureUtilisation.takeLast(5) else ligne.heureUtilisation

    Row(
        modifier = Modifier.fillMaxWidth().background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(couleurAv),
            contentAlignment = Alignment.Center
        ) { Text(icone, color = couleurIcone, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                ligne.nomClient.ifEmpty { "Inconnu" },
                color = colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val sousTitre = when (ligne.statut) {
                "refuse"  -> ligne.raisonRefus.ifEmpty { "QR invalide" }
                "doublon" -> "Ticket déjà utilisé"
                else      -> ligne.categorie
            }
            Text(
                "$sousTitre · $heure",
                color = couleurSub,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(bgBadge)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(labelBadge, color = txtBadge, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun StatItemHisto(
    valeur: Int,
    label: String,
    couleur: Color,
    modifier: Modifier = Modifier,
    colors: ColorScheme = MaterialTheme.colorScheme
) {
    Column(
        modifier = modifier.padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valeur.toString(), color = couleur, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = colors.onSurfaceVariant, fontSize = 11.sp)
    }
}
