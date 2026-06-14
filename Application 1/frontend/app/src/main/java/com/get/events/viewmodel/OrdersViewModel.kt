package com.get.events.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.get.events.data.api.OrderDto
import com.get.events.data.model.Ticket
import com.get.events.data.model.TicketStatus
import com.get.events.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrdersViewModel : ViewModel() {

    private val repository = EventRepository()

    private val _orders = MutableStateFlow<List<OrderDto>>(emptyList())
    val orders: StateFlow<List<OrderDto>> = _orders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _orders.value = repository.getMyOrders()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createContestRegistration(
        eventId: String,
        teamName: String,
        projectTheme: String,
        membersCount: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val eventIdInt = eventId.toIntOrNull()
        if (eventIdInt == null) {
            onError("ID événement invalide")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createContestRegistration(
                eventId      = eventIdInt,
                teamName     = teamName,
                projectTheme = projectTheme,
                membersCount = membersCount
            )
            _isLoading.value = false
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Erreur lors de l'inscription au concours") }
            )
        }
    }

    fun createOrder(
        eventId: String,
        paymentMethod: String,
        paymentReference: String? = null,
        ticketTypeId: String? = null,
        quantity: Int = 1,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val eventIdInt = eventId.toIntOrNull()
        if (eventIdInt == null) {
            onError("ID événement invalide")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createOrder(
                eventId = eventIdInt,
                quantity = quantity,
                paymentMethod = paymentMethod,
                paymentReference = paymentReference,
                ticketTypeId = ticketTypeId
            )
            _isLoading.value = false
            result.fold(
                onSuccess = {
                    loadOrders()
                    onSuccess()
                },
                onFailure = { onError(it.message ?: "Erreur lors de la commande") }
            )
        }
    }

    fun ordersAsTickets(): List<Ticket> = _orders.value
        .filter { !it.status.equals("CANCELLED", ignoreCase = true) }
        .map { order ->
        Ticket(
            id = order.id.toString(),
            eventId = order.eventId.toString(),
            eventTitle = order.eventTitle ?: "Événement",
            eventDate = order.createdAt,
            eventLocation = "",
            qrCode = order.qrCodeUrl ?: "",
            holderName = "",
            status = when (order.status.uppercase()) {
                "VALIDATED" -> TicketStatus.VALID
                "USED" -> TicketStatus.USED
                "PENDING" -> TicketStatus.PENDING
                "CANCELLED" -> TicketStatus.EXPIRED
                else -> TicketStatus.EXPIRED
            }
        )
    }

    fun getTicketById(ticketId: String): Ticket? =
        ordersAsTickets().find { it.id == ticketId }
}
