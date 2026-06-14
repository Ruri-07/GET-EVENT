package com.get.events.data.model

// ─── User ───────────────────────────────────────────────────────────────────
data class User(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val email: String
)

// ─── Event ──────────────────────────────────────────────────────────────────
data class Event(
    val id: String,
    val title: String,
    val category: String,           // "SOIRÉE", "CONCOURS", "VOYAGE", etc.
    val categoryEmoji: String,
    val dateLabel: String,          // "Vendredi 14 Juin 2025 — 19h00"
    val location: String,
    val totalPlaces: Int,
    val reservedPlaces: Int,
    val description: String,
    val imageUrl: String?,
    val backgroundColor: Long,      // ARGB hex for placeholder bg
    val isFeatured: Boolean = false,
    val hasTickets: Boolean = true,
    val isSaved: Boolean = false,
    val ticketPriceStudent: Double = 0.0,
    val ticketPriceTeacher: Double = 0.0
)

// ─── Ticket ──────────────────────────────────────────────────────────────────
data class Ticket(
    val id: String,
    val eventId: String,
    val eventTitle: String,
    val eventDate: String,
    val eventLocation: String,
    val qrCode: String,
    val holderName: String,
    val status: TicketStatus
)

enum class TicketStatus { PENDING, VALID, USED, EXPIRED }

// ─── Contest ─────────────────────────────────────────────────────────────────
data class Contest(
    val id: String,
    val eventId: String,
    val name: String,
    val dateLabel: String,
    val reservedForLabel: String,
    val isOpen: Boolean,
    val teamsCount: Int
)

data class ContestRegistrationRequest(
    val contestId: String,
    val teamName: String,
    val projectTheme: String,
    val membersCount: Int
)

// ─── Notification ────────────────────────────────────────────────────────────
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val timestamp: String
)

// ─── API Response wrapper ────────────────────────────────────────────────────
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
