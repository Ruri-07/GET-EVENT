package com.get.events.data.repository

import androidx.compose.ui.graphics.Color
import com.get.events.data.model.TicketType

/**
 * Extension de FakeData pour les types de tickets.
 * Ces données simulent GET /events/{id}/ticket-types
 */
object FakeTicketData {

    val ticketTypes = listOf(
        TicketType(
            id = "tt_etudiant",
            name = "Ticket Étudiant",
            subtitle = "Tarif préférentiel",
            priceAr = 5000,
            priceLabel = "5 000 Ar",
            emoji = "🎫",
            iconBgColor = Color(0xFFB7E4C7)   // mint vert
        ),
        TicketType(
            id = "tt_enseignant",
            name = "Ticket Enseignant",
            subtitle = "Accès VIP",
            priceAr = 10000,
            priceLabel = "10 000 Ar",
            emoji = "🏅",
            iconBgColor = Color(0xFFBBDEFB)   // bleu clair
        )
    )
}
