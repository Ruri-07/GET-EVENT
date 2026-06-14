package com.getevents

import com.getevents.models.Events
import com.getevents.models.Users
import com.getevents.utils.hashPassword
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Données initiales pour le développement et les premiers tests.
 */
fun Application.seedDatabase() {
    transaction {
        val userCount = Users.selectAll().count()
        if (userCount > 0L) {
            println("ℹ️  Base déjà initialisée ($userCount utilisateur(s))")
            return@transaction
        }

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        Users.insert {
            it[email] = "admin@get.mg"
            it[passwordHash] = hashPassword("admin123")
            it[fullName] = "Administrateur GET"
            it[phone] = null
            it[role] = "ADMIN"
            it[isApproved] = true
            it[registrationStatus] = "APPROVED"
        }
        Users.insert {
            it[email] = "demo@get.mg"
            it[passwordHash] = hashPassword("demo123")
            it[fullName] = "Utilisateur Démo"
            it[phone] = "0340000000"
            it[role] = "USER"
            it[isApproved] = true
            it[userType] = "Étudiant"
            it[mention] = "Télécommunications"
            it[year] = "L3"
            it[registrationStatus] = "APPROVED"
        }

        Events.insert {
            it[title] = "Grande Réception GET 2025"
            it[description] = "La grande réception annuelle du GET, une soirée de gala pour célébrer la fin d'année universitaire."
            it[date] = "2025-06-14"
            it[location] = "Salle de réception, Campus Ankatso"
            it[ticketPrice] = 5000.toBigDecimal()
            it[ticketPriceTeacher] = 10000.toBigDecimal()
            it[totalTickets] = 200
            it[remainingTickets] = 200
            it[imageUrl] = null
            it[createdBy] = 1
            it[createdAt] = now
        }
        Events.insert {
            it[title] = "Voyage GET Côte Est"
            it[description] = "Voyage découverte sur la côte est de Madagascar."
            it[date] = "2025-06-28"
            it[location] = "Départ Campus Ankatso"
            it[ticketPrice] = 10000.toBigDecimal()
            it[ticketPriceTeacher] = 20000.toBigDecimal()
            it[totalTickets] = 50
            it[remainingTickets] = 50
            it[imageUrl] = null
            it[createdBy] = 1
            it[createdAt] = now
        }
        Events.insert {
            it[title] = "Concours Mini-Projet GET"
            it[description] = "Concours inter-équipes sur des thèmes innovants : IoT, 5G, Signal..."
            it[date] = "2025-06-20"
            it[location] = "Amphithéâtre GET"
            it[ticketPrice] = 0.toBigDecimal()
            it[ticketPriceTeacher] = 0.toBigDecimal()
            it[totalTickets] = 100
            it[remainingTickets] = 100
            it[imageUrl] = null
            it[createdBy] = 1
            it[createdAt] = now
        }

        println("✅ Données de démo insérées")
        println("   Admin : admin@get.mg / admin123")
        println("   User  : demo@get.mg / demo123")
    }
}
