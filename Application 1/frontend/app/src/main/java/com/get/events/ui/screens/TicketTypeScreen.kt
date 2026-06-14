package com.get.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.get.events.data.model.TicketType
import com.get.events.data.repository.TicketTypeHelper
import com.get.events.ui.components.PurchaseStepper
import com.get.events.ui.theme.*
import com.get.events.viewmodel.EventsViewModel

@Composable
fun TicketTypeScreen(
    eventId: String,
    onBack: () -> Unit = {},
    onContinue: (ticketTypeId: String) -> Unit = {},
    eventsVm: EventsViewModel = viewModel()
) {
    LaunchedEffect(eventId) { eventsVm.loadEvent(eventId) }
    val event by eventsVm.selectedEvent.collectAsState()

    val ticketTypes = remember(event) {
        event?.let { TicketTypeHelper.ticketTypesForEvent(it) } ?: emptyList()
    }
    var selectedTypeId by remember(ticketTypes) {
        mutableStateOf(ticketTypes.firstOrNull()?.id)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retour",
                tint = TextPrimary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable { onBack() }
            )
            Text(
                text = "Achat de Ticket",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.height(24.dp))

        PurchaseStepper(
            currentStep = 1,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(28.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            if (event == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenDark)
                }
            } else {
                Text(
                    text = event!!.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${event!!.categoryEmoji} ${event!!.category} · Choisissez votre type de ticket",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(24.dp))

                if (ticketTypes.isEmpty()) {
                    Text(
                        text = "Aucun ticket payant disponible pour cet événement.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    ticketTypes.forEach { ticketType ->
                        val isSelected = selectedTypeId == ticketType.id
                        TicketTypeCard(
                            ticketType = ticketType,
                            isSelected = isSelected,
                            onClick = { selectedTypeId = ticketType.id }
                        )
                        Spacer(Modifier.height(14.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        selectedTypeId?.let { onContinue(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenDark,
                        contentColor = Color.White
                    ),
                    enabled = selectedTypeId != null
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Continuer vers le paiement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TicketTypeCard(
    ticketType: TicketType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .then(
                if (isSelected) Modifier.border(2.dp, GreenDark, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ticketType.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = ticketType.emoji, fontSize = 26.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticketType.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = ticketType.subtitle,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = ticketType.priceLabel,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = if (isSelected) GreenDark else TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.End
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(GreenDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Sélectionné",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
