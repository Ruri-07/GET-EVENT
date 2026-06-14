package com.getevents.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

// Ce que l'API va retourner au mobile
@Serializable
data class User(
    val id: Int = 0,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val phone: String? = null,
    val role: String = "USER",
    val isApproved: Boolean = false,
    val userType: String = "Étudiant",
    val mention: String = "Télécommunications",
    val year: String = "",
    val studentCardUrl: String? = null,
    val cin: String? = null,
    val registrationStatus: String = "PENDING"
)

// La table MySQL
object Users : Table("users") {
    val id                 = integer("id").autoIncrement()
    val email              = varchar("email", 255).uniqueIndex()
    val passwordHash       = varchar("password_hash", 255)
    val fullName           = varchar("full_name", 255)
    val phone              = varchar("phone", 50).nullable()
    val role               = varchar("role", 50).default("USER")
    val isApproved         = bool("is_approved").default(false)
    val userType           = varchar("user_type", 50).default("Étudiant")
    val mention            = varchar("mention", 100).default("Télécommunications")
    val year               = varchar("year", 20).default("")
    val studentCardUrl     = varchar("student_card_url", 500).nullable()
    val cin                = varchar("cin", 50).nullable()
    val registrationStatus = varchar("registration_status", 20).default("PENDING")

    override val primaryKey = PrimaryKey(id)
}
