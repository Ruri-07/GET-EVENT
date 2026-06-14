package com.getevents

import com.getevents.models.Users
import com.getevents.models.Events
import com.getevents.models.Orders
import com.getevents.models.PasswordResetTokens
import com.getevents.models.ContestRegistrations
import com.getevents.models.Notifications
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureExposed() {
    val dbMode = (System.getenv("DB_MODE") ?: "mysql").lowercase()
    val hikariConfig = HikariConfig().apply {
        when (dbMode) {
            "mysql" -> {
                driverClassName = "com.mysql.cj.jdbc.Driver"
                jdbcUrl = System.getenv("DB_URL")
                    ?: "jdbc:mysql://localhost:3306/getevents_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                username = System.getenv("DB_USER") ?: "root"
                password = System.getenv("DB_PASSWORD") ?: "mysql123"
            }
            else -> {
                driverClassName = "org.h2.Driver"
                jdbcUrl = System.getenv("DB_URL")
                    ?: "jdbc:h2:file:./data/getevents_db;MODE=MySQL;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"
                username = System.getenv("DB_USER") ?: "sa"
                password = System.getenv("DB_PASSWORD") ?: ""
            }
        }
        maximumPoolSize = 10
        isAutoCommit = false
        // Laisser HikariCP retenter pendant 60s avant d'abandonner
        initializationFailTimeout = 60000
    }

    // Retry manuel si MySQL pas encore prêt
    var dataSource: HikariDataSource? = null
    repeat(10) { attempt ->
        if (dataSource != null) return@repeat
        try {
            dataSource = HikariDataSource(hikariConfig)
        } catch (e: Exception) {
            println("⏳ Tentative ${attempt + 1}/10 — MySQL pas prêt, attente 5s...")
            Thread.sleep(5000)
        }
    }

    val ds = dataSource ?: throw IllegalStateException("❌ Impossible de se connecter à MySQL après 10 tentatives")

    Database.connect(ds)
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Users, Events, Orders, PasswordResetTokens, ContestRegistrations, Notifications
        )
    }
    seedDatabase()
    println("✅ Base $dbMode connectée — tables vérifiées")
}