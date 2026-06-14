package com.getevents.services

import com.getevents.models.*
import com.getevents.repositories.EventRepository
import com.getevents.repositories.OrderRepository
import com.getevents.repositories.UserRepository
import com.getevents.utils.QrCodeGenerator

class OrderService {
    private val orderRepo = OrderRepository()
    private val eventRepo = EventRepository()
    private val userRepo = UserRepository()
    private val notificationService = NotificationService()

    // Créer une commande
    suspend fun createOrder(userId: Int, request: CreateOrderRequest): Result<Int> {
        // 1. Vérifier que l'événement existe
        val event = eventRepo.findById(request.eventId)
        if (event == null) {
            return Result.failure(Exception("Événement non trouvé"))
        }

        // 2. Vérifier les tickets disponibles
        if (!eventRepo.hasEnoughTickets(request.eventId, request.quantity)) {
            return Result.failure(Exception("Plus assez de tickets disponibles"))
        }


        // 3. Calculer le montant total selon le type de ticket
        val unitPrice = when (request.ticketTypeId) {
            "tt_enseignant" -> event.ticketPriceTeacher
            else -> event.ticketPriceStudent
        }
        val totalAmount = unitPrice * request.quantity

        // 4. Créer la commande (status = PENDING — validation par l'admin)
        val orderId = orderRepo.create(
            userId = userId,
            eventId = request.eventId,
            quantity = request.quantity,
            totalAmount = totalAmount,
            paymentMethod = request.paymentMethod,
            paymentReference = request.paymentReference
        )

        return Result.success(orderId)
    }

    // Récupérer les commandes d'un utilisateur
    suspend fun getUserOrders(userId: Int): List<Order> {
        val orders = orderRepo.findByUserId(userId)
        // Enrichir avec le titre de l'événement
        return orders.map { order ->
            val event = eventRepo.findById(order.eventId)
            order.copy(eventTitle = event?.title ?: "Événement inconnu")
        }
    }

    // Récupérer une commande spécifique
    suspend fun getUserOrder(orderId: Int, userId: Int): Order? {
        val order = orderRepo.findById(orderId, userId) ?: return null
        val event = eventRepo.findById(order.eventId)
        return order.copy(eventTitle = event?.title ?: "Événement inconnu")
    }

    // Valider une commande (admin)
    suspend fun validateOrder(orderId: Int): Result<Unit> {
        // 1. Vérifier que la commande existe
        val order = orderRepo.findById(orderId) ?: return Result.failure(Exception("Commande non trouvée"))

        // 2. Vérifier qu'elle est en attente
        if (order.status != OrderStatus.PENDING) {
            return Result.failure(Exception("Cette commande ne peut pas être validée (statut: ${order.status})"))
        }
        // 3. Récupérer les infos de l'événement et de l'utilisateur
        val event = eventRepo.findById(order.eventId) ?: return Result.failure(Exception("Événement non trouvé"))
        val user = userRepo.findById(order.userId) ?: return Result.failure(Exception("Utilisateur non trouvé"))

        // 4. Générer le QR code
        val qrCodeUrl = QrCodeGenerator.generateTicketQrCode(
            orderId = orderId,
            eventTitle = event.title,
            userName = user.fullName,
            ticketCount = order.quantity
        )
        // 5. Mettre à jour la commande avec le QR code
        orderRepo.updateQrCode(orderId, qrCodeUrl)

        // 6. Valider la commande
        val updated = orderRepo.validate(orderId)
        if (!updated) {
            return Result.failure(Exception("Erreur lors de la validation"))
        }

        // 7. Réduire le nombre de tickets disponibles
        eventRepo.decrementRemainingTickets(order.eventId, order.quantity)

        notificationService.notify(
            userId  = order.userId,
            title   = "Paiement validé",
            message = "Votre paiement pour « ${event.title} » a été validé. Votre ticket est disponible.",
            type    = "ORDER_VALIDATED"
        )

        return Result.success(Unit)
    }

    // Annuler une commande (admin)
    suspend fun cancelOrder(orderId: Int): Result<Unit> {
        val order = orderRepo.findById(orderId) ?: return Result.failure(Exception("Commande non trouvée"))

        if (order.status != OrderStatus.PENDING) {
            return Result.failure(Exception("Cette commande ne peut pas être annulée"))
        }

        val updated = orderRepo.cancel(orderId)
        if (!updated) {
            return Result.failure(Exception("Erreur lors de l'annulation"))
        }

        val event = eventRepo.findById(order.eventId)
        notificationService.notify(
            userId  = order.userId,
            title   = "Paiement refusé",
            message = "Votre paiement pour « ${event?.title ?: "l'événement"} » a été refusé.",
            type    = "ORDER_CANCELLED"
        )

        return Result.success(Unit)
    }

    // Récupérer toutes les commandes (admin)
    suspend fun getAllOrders(): List<Order> {
        val orders = orderRepo.findAll()
        return orders.map { order ->
            val event = eventRepo.findById(order.eventId)
            order.copy(eventTitle = event?.title ?: "Événement inconnu")
        }
    }
}