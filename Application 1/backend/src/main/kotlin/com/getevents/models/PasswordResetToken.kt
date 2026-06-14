package com.getevents.models

import org.jetbrains.exposed.sql.Table

object PasswordResetTokens : Table("password_reset_tokens") {
    val id        = integer("id").autoIncrement()
    val email     = varchar("email", 255)
    val code      = varchar("code", 10)
    val expiresAt = long("expires_at")
    val used      = bool("used").default(false)
    val createdAt = varchar("created_at", 30)

    override val primaryKey = PrimaryKey(id)
}
