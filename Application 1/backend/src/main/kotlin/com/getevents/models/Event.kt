package com.getevents.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.time.LocalDate

// Ce que l'API va retourner au mobile
@Serializable
data class Event(
    val id: Int = 0,
    val title: String,
    val description: String,
    val date: String,           // Format ISO: "2025-12-25"
    val location: String,
    val ticketPriceStudent: Double,
    val ticketPriceTeacher: Double,
    val totalTickets: Int,
    val remainingTickets: Int,
    val imageUrl: String? = null,
    val createdBy: Int,         // ID de l'admin qui a créé
    val createdAt: String
)

// Requête pour créer/modifier un événement
@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val ticketPriceStudent: Double,
    val ticketPriceTeacher: Double,
    val totalTickets: Int,
    val imageUrl: String? = null
)

@Serializable
data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val location: String? = null,
    val ticketPriceStudent: Double? = null,
    val ticketPriceTeacher: Double? = null,
    val totalTickets: Int? = null,
    val imageUrl: String? = null
)

// Table MySQL
object Events : Table("events") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description")
    val date = varchar("date", 20)  // Stocké au format ISO
    val location = varchar("location", 255)
    val ticketPrice = decimal("ticket_price", 10, 2)              // Prix étudiant
    val ticketPriceTeacher = decimal("ticket_price_teacher", 10, 2).default(0.toBigDecimal())
    val totalTickets = integer("total_tickets")
    val remainingTickets = integer("remaining_tickets")
    val imageUrl = varchar("image_url", 500).nullable()
    val createdBy = integer("created_by")
    val createdAt = varchar("created_at", 30)  // Timestamp ISO

    override val primaryKey = PrimaryKey(id)
}
// Requête pour lister les événements (avec pagination et recherche)
@Serializable
data class GetEventsRequest(
    val search: String? = null,
    val page: Int = 1,
    val pageSize: Int = 10
)