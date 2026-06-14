package com.get.events.ui.components.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.get.events.ui.theme.GoldPrimary
import com.get.events.ui.theme.IconShieldBg

/**
 * Icône bouclier stylisée — identique à la maquette.
 * Fond arrondi vert foncé + bouclier doré.
 */
@Composable
fun ShieldAdminIcon(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(90.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(IconShieldBg)
            .padding(18.dp)
    ) {
        Icon(
            imageVector        = Icons.Default.Shield,
            contentDescription = "Accès Admin GET Telco",
            tint               = GoldPrimary,
            modifier           = Modifier.size(54.dp)
        )
    }
}
