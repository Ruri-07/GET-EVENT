package com.getevents.repositories

import com.getevents.models.PasswordResetTokens
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import java.time.Instant
import java.time.format.DateTimeFormatter

class PasswordResetRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun createToken(email: String, code: String, expiresAt: Long): Int = dbQuery {
        PasswordResetTokens.update({ PasswordResetTokens.email eq email }) {
            it[used] = true
        }
        PasswordResetTokens.insert {
            it[this.email]     = email
            it[this.code]      = code
            it[this.expiresAt] = expiresAt
            it[used]           = false
            it[createdAt]      = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        } get PasswordResetTokens.id
    }

    suspend fun findValidToken(email: String, code: String): Boolean = dbQuery {
        val now = System.currentTimeMillis()
        PasswordResetTokens.selectAll()
            .where {
                (PasswordResetTokens.email eq email) and
                (PasswordResetTokens.code eq code) and
                (PasswordResetTokens.used eq false) and
                (PasswordResetTokens.expiresAt greater now)
            }
            .any()
    }

    suspend fun markUsed(email: String, code: String): Boolean = dbQuery {
        PasswordResetTokens.update({
            (PasswordResetTokens.email eq email) and (PasswordResetTokens.code eq code)
        }) {
            it[used] = true
        } > 0
    }
}
