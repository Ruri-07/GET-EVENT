package com.get.events.ui.components.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.get.events.data.model.admin.EvenementProchain
import com.get.events.ui.theme.*

/**
 * Ligne d'un événement à venir dans la section "Prochains événements".
 */
@Composable
fun EvenementItem(
    evenement: EvenementProchain,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            // Icône calendrier
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F5E9))
            ) {
                Text(text = "📅", fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = evenement.titre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text     = "${evenement.dateDebut} · ${evenement.lieu}",
                    fontSize = 13.sp,
                    color    = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Badge inscrits / capacité
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GreenDark.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text       = "${evenement.nbInscrits}/${evenement.capaciteMax}",
                    color      = GreenDark,
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
