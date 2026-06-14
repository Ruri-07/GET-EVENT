package com.get.events.ui.components.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.data.model.admin.PaiementEnAttente
import com.get.events.ui.theme.*

/**
 * Ligne d'un paiement en attente dans la section "Paiements en attente".
 * Badge "En attente" orange fidèle à la maquette.
 */
@Composable
fun PaiementItem(
    paiement: PaiementEnAttente,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    onClick: () -> Unit = {}
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            // Icône money (emoji placeholder — remplacer par vrai asset si dispo)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0))
            ) {
                Text(text = "💸", fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Infos paiement
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "${paiement.nomClient} — ${formatMontant(paiement.montantAr)} Ar",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text     = "${paiement.modePaiement} · Réf: ${paiement.reference}",
                    fontSize = 13.sp,
                    color    = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Badge statut
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF5A623))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text       = "En attente",
                    color      = Color.White,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showDivider) {
            Divider(
                color     = DividerColor,
                thickness = 1.dp,
                modifier  = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/** Formate 5000 → "5 000" */
private fun formatMontant(montant: Int): String {
    return montant.toString()
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()
}
