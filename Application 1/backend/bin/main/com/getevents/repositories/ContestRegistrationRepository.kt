package com.getevents.repositories

import com.getevents.models.ContestRegistration
import com.getevents.models.ContestRegistrationStatus
import com.getevents.models.ContestRegistrations
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import java.time.Instant
import java.time.format.DateTimeFormatter

class ContestRegistrationRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(
        userId: Int,
        eventId: Int,
        teamName: String,
        projectTheme: String,
        membersCount: Int
    ): Int = dbQuery {
        ContestRegistrations.insert {
            it[this.userId]       = userId
            it[this.eventId]      = eventId
            it[this.teamName]     = teamName
            it[this.projectTheme] = projectTheme
            it[this.membersCount] = membersCount
            it[status]            = ContestRegistrationStatus.PENDING
            it[createdAt]         = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        } get ContestRegistrations.id
    }

    suspend fun findById(id: Int): ContestRegistration? = dbQuery {
        ContestRegistrations.selectAll()
            .where { ContestRegistrations.id eq id }
            .map { rowToRegistration(it) }
            .singleOrNull()
    }

    suspend fun findAllPending(): List<ContestRegistration> = dbQuery {
        ContestRegistrations.selectAll()
            .where { ContestRegistrations.status eq ContestRegistrationStatus.PENDING }
            .orderBy(ContestRegistrations.id to SortOrder.DESC)
            .map { rowToRegistration(it) }
    }

    suspend fun findAll(): List<ContestRegistration> = dbQuery {
        ContestRegistrations.selectAll()
            .orderBy(ContestRegistrations.id to SortOrder.DESC)
            .map { rowToRegistration(it) }
    }

    suspend fun countByEventId(eventId: Int): Int = dbQuery {
        ContestRegistrations.selectAll()
            .where { ContestRegistrations.eventId eq eventId }
            .count()
            .toInt()
    }

    suspend fun approve(id: Int): Boolean = dbQuery {
        ContestRegistrations.update({ ContestRegistrations.id eq id }) {
            it[status] = ContestRegistrationStatus.APPROVED
        } > 0
    }

    suspend fun reject(id: Int): Boolean = dbQuery {
        ContestRegistrations.update({ ContestRegistrations.id eq id }) {
            it[status] = ContestRegistrationStatus.REJECTED
        } > 0
    }

    private fun rowToRegistration(row: ResultRow) = ContestRegistration(
        id           = row[ContestRegistrations.id],
        userId       = row[ContestRegistrations.userId],
        eventId      = row[ContestRegistrations.eventId],
        teamName     = row[ContestRegistrations.teamName],
        projectTheme = row[ContestRegistrations.projectTheme],
        membersCount = row[ContestRegistrations.membersCount],
        status       = row[ContestRegistrations.status],
        createdAt    = row[ContestRegistrations.createdAt]
    )
}
