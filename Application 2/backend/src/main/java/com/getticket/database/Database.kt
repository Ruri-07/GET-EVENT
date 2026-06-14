package com.getticket.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

private fun env(name: String, default: String) =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: default

/**
 * Connexion à la même base que GET Events (Application 1).
 * Variables d'environnement : DB_MODE, DB_URL, DB_USER, DB_PASSWORD
 */
fun connecterBDD() {
    val dbMode = env("DB_MODE", "mysql").lowercase()

    val config = HikariConfig().apply {
        when (dbMode) {
            "mysql" -> {
                driverClassName = "com.mysql.cj.jdbc.Driver"
                jdbcUrl = env(
                    "DB_URL",
                    "jdbc:mysql://localhost:3306/getevents_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                )
                username = env("DB_USER", "root")
                password = env("DB_PASSWORD", "mysql123")
            }
            else -> {
                driverClassName = "org.h2.Driver"
                jdbcUrl = env(
                    "DB_URL",
                    "jdbc:h2:file:../Application 1/data/getevents_db;MODE=MySQL;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"
                )
                username = env("DB_USER", "sa")
                password = env("DB_PASSWORD", "")
            }
        }
        maximumPoolSize = 10
        isAutoCommit = false
    }

    try {
        Database.connect(HikariDataSource(config))
    } catch (e: Exception) {
        System.err.println(
            """
            |
            |❌ Connexion impossible à la base GET Events (getevents_db).
            |   Mode : $dbMode
            |   URL  : ${config.jdbcUrl}
            |
            |→ Vérifiez que MySQL tourne et que Application 1 a initialisé la base.
            |  Exemple PowerShell :
            |    ${'$'}env:DB_PASSWORD="votre_mot_de_passe"
            |
            |Erreur : ${e.cause?.message ?: e.message}
            """.trimMargin()
        )
        throw e
    }

    transaction {
        // Table dédiée au scanner — ne modifie pas le schéma App 1
        SchemaUtils.createMissingTablesAndColumns(ScanHistory)
    }

    println("✅ GET Ticket connecté à getevents_db (mode $dbMode)")
}

// ── Tables partagées avec GET Events (Application 1) ─────────

object Users : Table("users") {
    val id           = integer("id").autoIncrement()
    val email        = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName     = varchar("full_name", 255)
    val phone        = varchar("phone", 50).nullable()
    val role         = varchar("role", 50).default("USER")
    val isApproved   = bool("is_approved").default(false)
    val userType     = varchar("user_type", 50).default("Étudiant")
    val mention      = varchar("mention", 100).default("Télécommunications")
    val year         = varchar("year", 20).default("")
    override val primaryKey = PrimaryKey(id)
}

object Events : Table("events") {
    val id       = integer("id").autoIncrement()
    val title    = varchar("title", 255)
    val date     = varchar("date", 20)
    val location = varchar("location", 255)
    override val primaryKey = PrimaryKey(id)
}

object Orders : Table("orders") {
    val id               = integer("id").autoIncrement()
    val userId           = integer("user_id").references(Users.id)
    val eventId          = integer("event_id")
    val quantity         = integer("quantity")
    val status           = varchar("status", 20).default("PENDING")
    val paymentMethod    = varchar("payment_method", 50)
    val qrCodeUrl        = varchar("qr_code_url", 500).nullable()
    val createdAt        = varchar("created_at", 30)
    override val primaryKey = PrimaryKey(id)
}

// ── Historique des scans (propre à GET Ticket) ─────────────────

object ScanHistory : Table("scan_history") {
    val id           = integer("id").autoIncrement()
    val orderId      = integer("order_id").nullable()
    val clientName   = varchar("client_name", 255).default("Inconnu")
    val category     = varchar("category", 100).default("")
    val status       = varchar("status", 20)
    val rejectReason = varchar("reject_reason", 200).default("")
    val scannedAt    = varchar("scanned_at", 50)
    override val primaryKey = PrimaryKey(id)
}
