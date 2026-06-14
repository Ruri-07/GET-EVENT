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
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Users, Events, Orders, PasswordResetTokens, ContestRegistrations, Notifications
        )
    }

    seedDatabase()

    println("✅ Base $dbMode connectée — tables vérifiées")
}
