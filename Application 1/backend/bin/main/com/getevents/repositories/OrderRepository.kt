package com.getevents.repositories

import com.getevents.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OrderRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) { block() }
    }

    // Créer une commande
    suspend fun create(
        userId: Int,
        eventId: Int,
        quantity: Int,
        totalAmount: Double,
        paymentMethod: String,
        paymentReference: String? = null
    ): Int = dbQuery {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        Orders.insert {
            it[this.userId] = userId
            it[this.eventId] = eventId
            it[this.quantity] = quantity
            it[this.totalAmount] = totalAmount.toBigDecimal()
            it[this.paymentMethod] = paymentMethod
            it[this.paymentReference] = paymentReference
            it[this.createdAt] = now
        } get Orders.id
    }

    // Récupérer les commandes d'un utilisateur
    suspend fun findByUserId(userId: Int): List<Order> = dbQuery {
        Orders.selectAll()
            .where { Orders.userId eq userId }
            .orderBy(Orders.id to SortOrder.DESC)
            .map { rowToOrder(it) }
    }

    // Récupérer une commande par ID (avec vérification utilisateur)
    suspend fun findById(orderId: Int, userId: Int? = null): Order? = dbQuery {
        var query = Orders.selectAll().where { Orders.id eq orderId }

        userId?.let {
            query = query.andWhere { Orders.userId eq it }
        }

        query.map { rowToOrder(it) }.singleOrNull()
    }

    // Récupérer toutes les commandes (admin)
    suspend fun findAll(): List<Order> = dbQuery {
        Orders.selectAll()
            .orderBy(Orders.id to SortOrder.DESC)
            .map { rowToOrder(it) }
    }

    // Valider une commande (admin) - génère QR code plus tard
    suspend fun validate(orderId: Int): Boolean = dbQuery {
        val updated = Orders.update({ Orders.id eq orderId }) {
            it[status] = OrderStatus.VALIDATED
        }
        updated > 0
    }

    // Annuler une commande
    suspend fun cancel(orderId: Int): Boolean = dbQuery {
        val updated = Orders.update({ Orders.id eq orderId }) {
            it[status] = OrderStatus.CANCELLED
        }
        updated > 0
    }

    // Mettre à jour le QR code après génération
    suspend fun updateQrCode(orderId: Int, qrCodeUrl: String): Boolean = dbQuery {
        val updated = Orders.update({ Orders.id eq orderId }) {
            it[this.qrCodeUrl] = qrCodeUrl
        }
        updated > 0
    }

    // Statistiques pour admin
    suspend fun getTotalRevenue(): Double = dbQuery {
        Orders.selectAll()
            .where { Orders.status eq OrderStatus.VALIDATED }
            .map { it[Orders.totalAmount].toDouble() }
            .sum()
            ?: 0.0
    }

    suspend fun getPendingOrdersCount(): Int = dbQuery {
        Orders.selectAll()
            .where { Orders.status eq OrderStatus.PENDING }
            .count()
            .toInt()
    }

    // Conversion
    private fun rowToOrder(row: ResultRow): Order {
        return Order(
            id = row[Orders.id],
            userId = row[Orders.userId],
            eventId = row[Orders.eventId],
            eventTitle = null,  // Sera rempli par le service si besoin
            quantity = row[Orders.quantity],
            totalAmount = row[Orders.totalAmount].toDouble(),
            status = row[Orders.status],
            paymentMethod = row[Orders.paymentMethod],
            paymentReference = row[Orders.paymentReference],
            qrCodeUrl = row[Orders.qrCodeUrl],
            createdAt = row[Orders.createdAt]
        )
    }
}