package com.get.events.data.model

import androidx.compose.ui.graphics.Color

// ─── Ticket type (Étudiant / Enseignant / VIP…) ──────────────────────────────
data class TicketType(
    val id: String,
    val name: String,
    val subtitle: String,
    val priceAr: Int,               // Prix en Ariary
    val priceLabel: String,         // "5 000 Ar"
    val emoji: String,
    val iconBgColor: Color
)

// ─── Mobile Money operators ───────────────────────────────────────────────────
enum class MobileOperator {
    ORANGE_MONEY,
    MVOLA,
    AIRTEL_MONEY;

    val displayName: String get() = when (this) {
        ORANGE_MONEY -> "Orange Money"
        MVOLA -> "MVola"
        AIRTEL_MONEY -> "Airtel Money"
    }

    val operatorPhone: String get() = when (this) {
        ORANGE_MONEY -> "032 XX XXX XX"
        MVOLA -> "034 XX XXX XX"
        AIRTEL_MONEY -> "033 XX XXX XX"
    }
}

// ─── Payment request (envoyé au back end) ─────────────────────────────────────
data class MobilePaymentRequest(
    val eventId: String,
    val ticketTypeId: String,
    val quantity: Int = 1,
    val operator: MobileOperator,
    val studentName: String,
    val phoneNumber: String,
    val transactionRef: String
)

// ─── Payment response ─────────────────────────────────────────────────────────
data class PaymentResult(
    val success: Boolean,
    val ticketId: String?,
    val message: String
)
