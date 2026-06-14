package com.get.events.data.repository

import androidx.compose.ui.graphics.Color
import com.get.events.data.model.Event
import com.get.events.data.model.TicketType

object TicketTypeHelper {

    fun formatPriceAr(amount: Int): String {
        val formatted = String.format("%,d", amount).replace(',', '\u00A0')
        return "$formatted Ar"
    }

    fun ticketTypesForEvent(event: Event): List<TicketType> {
        val studentPrice = event.ticketPriceStudent.toInt()
        val teacherPrice = event.ticketPriceTeacher.toInt()
        if (studentPrice <= 0 && teacherPrice <= 0) return emptyList()

        val labels = when (event.category.uppercase()) {
            "VOYAGE" -> TicketLabels(
                studentName = "Ticket Voyage Étudiant",
                teacherName = "Ticket Voyage Enseignant",
                studentSubtitle = "Participation au voyage",
                teacherSubtitle = "Participation VIP au voyage",
                studentEmoji = "🚌",
                teacherEmoji = "🎒"
            )
            "CONFÉRENCE" -> TicketLabels(
                studentName = "Ticket Conférence Étudiant",
                teacherName = "Ticket Conférence Enseignant",
                studentSubtitle = "Accès conférence",
                teacherSubtitle = "Accès conférence VIP",
                studentEmoji = "📡",
                teacherEmoji = "🎤"
            )
            else -> TicketLabels(
                studentName = "Ticket Étudiant",
                teacherName = "Ticket Enseignant",
                studentSubtitle = "Tarif préférentiel",
                teacherSubtitle = "Accès VIP",
                studentEmoji = "🎫",
                teacherEmoji = "🏅"
            )
        }

        val types = mutableListOf<TicketType>()
        if (studentPrice > 0) {
            types += TicketType(
                id = "tt_etudiant",
                name = labels.studentName,
                subtitle = labels.studentSubtitle,
                priceAr = studentPrice,
                priceLabel = formatPriceAr(studentPrice),
                emoji = labels.studentEmoji,
                iconBgColor = Color(0xFFB7E4C7)
            )
        }
        if (teacherPrice > 0) {
            types += TicketType(
                id = "tt_enseignant",
                name = labels.teacherName,
                subtitle = labels.teacherSubtitle,
                priceAr = teacherPrice,
                priceLabel = formatPriceAr(teacherPrice),
                emoji = labels.teacherEmoji,
                iconBgColor = Color(0xFFBBDEFB)
            )
        }
        return types
    }

    private data class TicketLabels(
        val studentName: String,
        val teacherName: String,
        val studentSubtitle: String,
        val teacherSubtitle: String,
        val studentEmoji: String,
        val teacherEmoji: String
    )
}
