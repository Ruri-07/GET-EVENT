package com.get.events.data.repository

import com.get.events.data.api.*
import com.get.events.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * EventRepository — couche entre les ViewModels et l'API Ktor backend.
 * Convertit les DTOs backend en modèles UI.
 */
class EventRepository {

    private val api = RetrofitClient.instance

    // ── Events ───────────────────────────────────────────────────────────────

    suspend fun getAllEvents(): List<Event> {
        return try {
            api.getEvents(pageSize = 50)
                .map { it.toUiModel() }
                .distinctBy { it.id }
        } catch (e: Exception) {
            (FakeData.featuredEvents + FakeData.upcomingEvents).distinctBy { it.id }
        }
    }

    fun getFeaturedEvents(): Flow<List<Event>> = flow {
        val all = getAllEvents()
        val featured = all.filter { it.isFeatured }.ifEmpty { all.take(2) }
        emit(featured.map { it.copy(isFeatured = true) })
    }

    fun getUpcomingEvents(): Flow<List<Event>> = flow {
        emit(getAllEvents())
    }

    suspend fun searchEvents(query: String): List<Event> {
        return try {
            api.getEvents(search = query).map { it.toUiModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getEventById(id: String): Flow<Event?> = flow {
        try {
            val idInt = id.toIntOrNull()
            if (idInt != null) {
                emit(api.getEvent(idInt).toUiModel())
            } else {
                emit(FakeData.featuredEvents.find { it.id == id }
                    ?: FakeData.upcomingEvents.find { it.id == id })
            }
        } catch (e: Exception) {
            emit(FakeData.featuredEvents.find { it.id == id }
                ?: FakeData.upcomingEvents.find { it.id == id })
        }
    }

    // ── Orders ───────────────────────────────────────────────────────────────

    suspend fun createContestRegistration(
        eventId: Int,
        teamName: String,
        projectTheme: String,
        membersCount: Int
    ): Result<Int> {
        return try {
            val response = api.createContestRegistration(
                CreateContestRegistrationRequest(
                    eventId      = eventId,
                    teamName     = teamName,
                    projectTheme = projectTheme,
                    membersCount = membersCount
                )
            )
            Result.success(response.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrder(
        eventId: Int,
        quantity: Int,
        paymentMethod: String,
        paymentReference: String? = null,
        ticketTypeId: String? = null
    ): Result<Int> {
        return try {
            val response = api.createOrder(
                CreateOrderRequest(
                    eventId          = eventId,
                    quantity         = quantity,
                    paymentMethod    = paymentMethod,
                    paymentReference = paymentReference,
                    ticketTypeId     = ticketTypeId
                )
            )
            Result.success(response.orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyOrders(): List<OrderDto> {
        return try {
            api.getMyOrders()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Contest (stub — pas encore dans backend) ──────────────────────────────

    fun getContest(eventId: String): Flow<Contest?> = flow {
        emit(FakeData.contests.find { it.eventId == eventId })
    }

    suspend fun registerContest(request: ContestRegistrationRequest): Result<Unit> {
        return Result.success(Unit) // TODO: endpoint backend contest
    }

    fun getCurrentUser(): Flow<User> = flow {
        emit(FakeData.currentUser)
    }

    fun getUnreadNotificationCount(): Flow<Int> = flow { emit(0) }

    suspend fun buyTicket(eventId: String): Result<Ticket> {
        return Result.success(FakeData.sampleTicket)
    }

    suspend fun toggleSaveEvent(eventId: String, save: Boolean) { /* TODO */ }
}

// ── Extension: EventDto → UI Event ───────────────────────────────────────────

private fun EventDto.toUiModel(isFeatured: Boolean = false): Event {
    val categoryInfo = when {
        title.contains("concours", ignoreCase = true)  -> Pair("CONCOURS", "🏆")
        title.contains("voyage", ignoreCase = true)    -> Pair("VOYAGE", "🚌")
        title.contains("conférence", ignoreCase = true)-> Pair("CONFÉRENCE", "📡")
        else                                           -> Pair("SOIRÉE", "🎉")
    }
    return Event(
        id               = id.toString(),
        title            = title,
        category         = categoryInfo.first,
        categoryEmoji    = categoryInfo.second,
        dateLabel        = date,
        location         = location,
        totalPlaces      = totalTickets,
        reservedPlaces   = totalTickets - remainingTickets,
        description      = description,
        imageUrl         = imageUrl,
        backgroundColor  = 0xFF1B4332,
        isFeatured       = isFeatured,
        hasTickets         = ticketPriceStudent > 0 || ticketPriceTeacher > 0,
        ticketPriceStudent = ticketPriceStudent,
        ticketPriceTeacher = ticketPriceTeacher
    )
}

// ─── Fake data pour le développement UI ─────────────────────────────────────
object FakeData {

    val currentUser = User(
        id = "u001",
        name = "Anjara",
        avatarUrl = null,
        email = "anjara@get.mg"
    )

    val featuredEvents = listOf(
        Event(
            id = "e001",
            title = "Grande Réception GET 2025",
            category = "SOIRÉE",
            categoryEmoji = "🎉",
            dateLabel = "Vendredi 14 Juin 2025 — 19h00",
            location = "Salle de réception, Campus Ankatso",
            totalPlaces = 200,
            reservedPlaces = 120,
            description = "La grande réception annuelle du GET, une soirée de gala pour célébrer la fin d'année universitaire.",
            imageUrl = null,
            backgroundColor = 0xFF1B4332,
            isFeatured = true,
            hasTickets = true
        ),
        Event(
            id = "e002",
            title = "Voyage GET Côte Est",
            category = "VOYAGE",
            categoryEmoji = "🚌",
            dateLabel = "Samedi 28 Juin 2025 — 06h00",
            location = "Départ Campus Ankatso",
            totalPlaces = 50,
            reservedPlaces = 30,
            description = "Voyage découverte sur la côte est de Madagascar.",
            imageUrl = null,
            backgroundColor = 0xFF1565C0,
            isFeatured = true,
            hasTickets = true
        )
    )

    val upcomingEvents = listOf(
        Event(
            id = "e003",
            title = "Concours Mini-Projet GET",
            category = "CONCOURS",
            categoryEmoji = "🏆",
            dateLabel = "Vendredi 20 Juin 2025",
            location = "Amphithéâtre GET",
            totalPlaces = 100,
            reservedPlaces = 36,
            description = "Concours inter-équipes sur des thèmes innovants : IoT, 5G, Signal...",
            imageUrl = null,
            backgroundColor = 0xFF6D4C41,
            hasTickets = false
        )
    )

    val contests = listOf(
        Contest(
            id = "c001",
            eventId = "e003",
            name = "Concours Mini-Projet GET",
            dateLabel = "20 Juin 2025",
            reservedForLabel = "Réservé aux étudiants de la mention",
            isOpen = true,
            teamsCount = 12
        )
    )

    val sampleTicket = Ticket(
        id = "t001",
        eventId = "e001",
        eventTitle = "Grande Réception GET 2025",
        eventDate = "Vendredi 14 Juin 2025 — 19h00",
        eventLocation = "Salle de réception, Campus Ankatso",
        qrCode = "QR_T001_GET2025",
        holderName = "Anjara",
        status = TicketStatus.VALID
    )

    val ticketTypes = FakeTicketData.ticketTypes

    fun getPendingTicketInfo(eventId: String): com.get.events.ui.screens.PendingTicketInfo {
        val event = featuredEvents.find { it.id == eventId }
            ?: upcomingEvents.find { it.id == eventId }
        val isContest = event?.category.equals("CONCOURS", ignoreCase = true)
            || event?.title?.contains("concours", ignoreCase = true) == true
        return com.get.events.ui.screens.PendingTicketInfo(
            eventOrganizer = "GET · Télécommunications",
            eventTitle     = event?.title ?: "Événement GET",
            eventDateLabel = event?.dateLabel ?: "Date à confirmer",
            isContest      = isContest
        )
    }
}
