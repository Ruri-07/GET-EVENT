package com.getevents.repositories

import com.getevents.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) { block() }
    }

    suspend fun create(request: CreateEventRequest, adminId: Int): Int = dbQuery {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        Events.insert {
            it[title] = request.title
            it[description] = request.description
            it[date] = request.date
            it[location] = request.location
            it[ticketPrice] = request.ticketPriceStudent.toBigDecimal()
            it[ticketPriceTeacher] = request.ticketPriceTeacher.toBigDecimal()
            it[totalTickets] = request.totalTickets
            it[remainingTickets] = request.totalTickets
            it[imageUrl] = request.imageUrl
            it[createdBy] = adminId
            it[createdAt] = now
        } get Events.id
    }

    suspend fun findAll(searchTerm: String? = null, page: Int = 1, pageSize: Int = 10): List<Event> = dbQuery {
        var query = Events.selectAll()

        searchTerm?.takeIf { it.isNotBlank() }?.let { term ->
            val likePattern = "%$term%"
            query = query.where {
                (Events.title like likePattern) or
                        (Events.description like likePattern) or
                        (Events.location like likePattern)
            }
        }

        query
            .limit(pageSize, (page - 1) * pageSize.toLong())
            .orderBy(Events.date to SortOrder.ASC)
            .map { rowToEvent(it) }
    }

    suspend fun findById(id: Int): Event? = dbQuery {
        Events.selectAll()
            .where { Events.id eq id }
            .map { rowToEvent(it) }
            .singleOrNull()
    }

    suspend fun update(id: Int, request: UpdateEventRequest): Boolean = dbQuery {
        val updated = Events.update({ Events.id eq id }) { row ->
            request.title?.let { row[Events.title] = it }
            request.description?.let { row[Events.description] = it }
            request.date?.let { row[Events.date] = it }
            request.location?.let { row[Events.location] = it }
            request.ticketPriceStudent?.let { row[Events.ticketPrice] = it.toBigDecimal() }
            request.ticketPriceTeacher?.let { row[Events.ticketPriceTeacher] = it.toBigDecimal() }
            request.totalTickets?.let { row[Events.totalTickets] = it }
            request.imageUrl?.let { row[Events.imageUrl] = it }
        }
        updated > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Events.deleteWhere { Events.id eq id } > 0
    }

    suspend fun decrementRemainingTickets(eventId: Int, quantity: Int): Boolean = dbQuery {
        Events.update({ Events.id eq eventId }) {
            it[remainingTickets] = remainingTickets - quantity
        } > 0
    }

    suspend fun hasEnoughTickets(eventId: Int, quantity: Int): Boolean = dbQuery {
        Events.selectAll()
            .where { Events.id eq eventId }
            .map { it[Events.remainingTickets] >= quantity }
            .singleOrNull() ?: false
    }

    private fun rowToEvent(row: ResultRow): Event {
        val studentPrice = row[Events.ticketPrice].toDouble()
        val teacherPrice = row[Events.ticketPriceTeacher].toDouble().let { stored ->
            if (stored > 0.0) stored else if (studentPrice > 0.0) studentPrice * 2 else 0.0
        }
        return Event(
            id = row[Events.id],
            title = row[Events.title],
            description = row[Events.description],
            date = row[Events.date],
            location = row[Events.location],
            ticketPriceStudent = studentPrice,
            ticketPriceTeacher = teacherPrice,
            totalTickets = row[Events.totalTickets],
            remainingTickets = row[Events.remainingTickets],
            imageUrl = row[Events.imageUrl],
            createdBy = row[Events.createdBy],
            createdAt = row[Events.createdAt]
        )
    }
}
