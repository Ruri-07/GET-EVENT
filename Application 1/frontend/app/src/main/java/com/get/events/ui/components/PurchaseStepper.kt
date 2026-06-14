package com.get.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
 * Stepper 3 étapes pour le tunnel d'achat de ticket.
 * currentStep : 1 = Choix ticket, 2 = Paiement, 3 = Confirmation
 */
@Composable
fun PurchaseStepper(
    currentStep: Int,
    totalSteps: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            val isDone = step < currentStep
            val isCurrent = step == currentStep
            val isPending = step > currentStep

            // Step circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .then(
                        when {
                            isDone -> Modifier.background(GreenDark)
                            isCurrent -> Modifier
                                .background(SurfaceWhite)
                                .border(2.dp, GreenDark, CircleShape)
                            else -> Modifier.background(Color(0xFFCCCCCC))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Étape $step complète",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = step.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isCurrent) GreenDark else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            // Connector line (except after last step)
            if (step < totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (step < currentStep) GreenDark else Color(0xFFCCCCCC)
                        )
                )
            }
        }
    }
}
