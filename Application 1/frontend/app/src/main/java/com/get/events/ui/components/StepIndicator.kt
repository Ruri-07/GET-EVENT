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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Indicateur de progression — utilisé dans RegisterStep1/2/3.
 * @param currentStep  étape active (1, 2 ou 3)
 */
@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int = 3
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalSteps) { index ->
            val stepNum  = index + 1
            val isDone   = stepNum < currentStep
            val isActive = stepNum == currentStep

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = when {
                            isDone   -> MaterialTheme.colorScheme.primary
                            isActive -> MaterialTheme.colorScheme.primaryContainer
                            else     -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    )
                    .then(
                        if (isActive) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector        = Icons.Default.Check,
                        contentDescription = "Étape $stepNum terminée",
                        tint               = Color.White,
                        modifier           = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text       = "$stepNum",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = when {
                            isActive -> MaterialTheme.colorScheme.onPrimaryContainer
                            else     -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (isDone) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}
