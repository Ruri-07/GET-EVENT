package com.getevents.repositories

import com.getevents.models.NotificationDto
import com.getevents.models.Notifications
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import java.time.Instant
import java.time.format.DateTimeFormatter

class NotificationRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(userId: Int, title: String, message: String, type: String): Int = dbQuery {
        Notifications.insert {
            it[this.userId]    = userId
            it[this.title]     = title
            it[this.message]   = message
            it[this.type]      = type
            it[isRead]         = false
            it[createdAt]      = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        } get Notifications.id
    }

    suspend fun findByUserId(userId: Int): List<NotificationDto> = dbQuery {
        Notifications.selectAll()
            .where { Notifications.userId eq userId }
            .orderBy(Notifications.id to SortOrder.DESC)
            .map { row ->
                NotificationDto(
                    id        = row[Notifications.id],
                    title     = row[Notifications.title],
                    message   = row[Notifications.message],
                    type      = row[Notifications.type],
                    isRead    = row[Notifications.isRead],
                    createdAt = row[Notifications.createdAt]
                )
            }
    }

    suspend fun countUnread(userId: Int): Int = dbQuery {
        Notifications.selectAll()
            .where { (Notifications.userId eq userId) and (Notifications.isRead eq false) }
            .count()
            .toInt()
    }

    suspend fun markAsRead(id: Int, userId: Int): Boolean = dbQuery {
        Notifications.update({
            (Notifications.id eq id) and (Notifications.userId eq userId)
        }) {
            it[isRead] = true
        } > 0
    }

    suspend fun markAllAsRead(userId: Int): Boolean = dbQuery {
        Notifications.update({ Notifications.userId eq userId }) {
            it[isRead] = true
        } >= 0
    }
}
