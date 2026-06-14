package com.getevents.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal

// Statuts possibles d'une commande
@kotlinx.serialization.Serializable
enum class OrderStatus {
    PENDING,    // En attente de validation admin
    VALIDATED,  // Validée, ticket valide
    CANCELLED,  // Annulée
    USED        // Ticket utilisé à l'entrée
}

// Ce que l'API retourne
@Serializable
data class Order(
    val id: Int = 0,
    val userId: Int,
    val eventId: Int,
    val eventTitle: String? = null,  // Pour affichage sans jointure
    val quantity: Int,
    val totalAmount: Double,
    val status: OrderStatus,
    val paymentMethod: String,
    val paymentReference: String? = null,
    val qrCodeUrl: String? = null,
    val createdAt: String
)

// Requête pour créer une commande
@Serializable
data class CreateOrderRequest(
    val eventId: Int,
    val quantity: Int,
    val paymentMethod: String,  // "MTN_MOMO", "ORANGE_MONEY", "MVOLA", "CONCOURS"
    val paymentReference: String? = null,
    val ticketTypeId: String? = null
)

// Table MySQL
object Orders : Table("orders") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val eventId = integer("event_id")
    val quantity = integer("quantity")
    val totalAmount = decimal("total_amount", 10, 2)
    val status = enumeration<OrderStatus>("status").default(OrderStatus.PENDING)
    val paymentMethod = varchar("payment_method", 50)
    val paymentReference = varchar("payment_reference", 255).nullable()
    val qrCodeUrl = varchar("qr_code_url", 500).nullable()
    val createdAt = varchar("created_at", 30)

    override val primaryKey = PrimaryKey(id)
}