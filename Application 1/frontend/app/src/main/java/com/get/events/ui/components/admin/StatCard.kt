package com.get.events.ui.components.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.ui.theme.*

/**
 * Carte statistique du Dashboard (Événements / En attente / Inscrits / Validés).
 *
 * @param value      Valeur numérique affichée en grand
 * @param label      Libellé sous la valeur (ex: "ÉVÉNEMENTS")
 * @param valueColor Couleur de la valeur (vert clair par défaut, or pour "en attente")
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color(0xFF80CBC4),   // vert menthe clair (défaut)
    backgroundColor: Color = Color(0xFF2A6E55),
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text       = value,
                color      = valueColor,
                fontSize   = 38.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 42.sp
            )
            Text(
                text       = label,
                color      = TextOnGreen.copy(alpha = 0.80f),
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
        }
    }
}
