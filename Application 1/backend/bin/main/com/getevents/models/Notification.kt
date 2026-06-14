package com.getevents.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class NotificationDto(
    val id: Int,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean = false,
    val createdAt: String
)

object Notifications : Table("notifications") {
    val id        = integer("id").autoIncrement()
    val userId    = integer("user_id")
    val title     = varchar("title", 255)
    val message   = text("message")
    val type      = varchar("type", 50)
    val isRead    = bool("is_read").default(false)
    val createdAt = varchar("created_at", 30)

    override val primaryKey = PrimaryKey(id)
}
