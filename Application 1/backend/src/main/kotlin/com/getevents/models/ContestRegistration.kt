package com.getevents.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
enum class ContestRegistrationStatus {
    PENDING, APPROVED, REJECTED
}

@Serializable
data class ContestRegistration(
    val id: Int = 0,
    val userId: Int,
    val userName: String? = null,
    val userEmail: String? = null,
    val eventId: Int,
    val eventTitle: String? = null,
    val teamName: String,
    val projectTheme: String,
    val membersCount: Int,
    val status: ContestRegistrationStatus = ContestRegistrationStatus.PENDING,
    val createdAt: String
)

@Serializable
data class CreateContestRegistrationRequest(
    val eventId: Int,
    val teamName: String,
    val projectTheme: String,
    val membersCount: Int
)

object ContestRegistrations : Table("contest_registrations") {
    val id           = integer("id").autoIncrement()
    val userId       = integer("user_id")
    val eventId      = integer("event_id")
    val teamName     = varchar("team_name", 255)
    val projectTheme = varchar("project_theme", 255)
    val membersCount = integer("members_count")
    val status       = enumeration<ContestRegistrationStatus>("status").default(ContestRegistrationStatus.PENDING)
    val createdAt    = varchar("created_at", 30)

    override val primaryKey = PrimaryKey(id)
}
