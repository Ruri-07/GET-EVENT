package com.getevents.repositories

import com.getevents.models.User
import com.getevents.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers

class UserRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) { block() }
    }

    suspend fun create(
        email: String,
        passwordHash: String,
        fullName: String,
        phone: String? = null,
        role: String = "USER",
        isApproved: Boolean = false,
        userType: String = "Étudiant",
        mention: String = "Télécommunications",
        year: String = "",
        studentCardUrl: String? = null,
        cin: String? = null,
        registrationStatus: String = "PENDING"
    ): Int = dbQuery {
        Users.insert {
            it[this.email]              = email
            it[this.passwordHash]       = passwordHash
            it[this.fullName]           = fullName
            it[this.phone]              = phone
            it[this.role]               = role
            it[this.isApproved]         = isApproved
            it[this.userType]           = userType
            it[this.mention]            = mention
            it[this.year]               = year
            it[this.studentCardUrl]     = studentCardUrl
            it[this.cin]                = cin
            it[this.registrationStatus] = registrationStatus
        } get Users.id
    }

    suspend fun updatePassword(id: Int, passwordHash: String): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[this.passwordHash] = passwordHash
        } > 0
    }

    suspend fun updateStudentCardUrl(id: Int, url: String): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[studentCardUrl] = url
        } > 0
    }

    suspend fun updateProfile(
        id: Int,
        fullName: String,
        phone: String?,
        mention: String?,
        year: String?
    ): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[this.fullName] = fullName
            it[this.phone] = phone
            if (mention != null) it[this.mention] = mention
            if (year != null) it[this.year] = year
        } > 0
    }

    suspend fun approveUser(id: Int): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[isApproved] = true
            it[registrationStatus] = "APPROVED"
        } > 0
    }

    suspend fun rejectUser(id: Int): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[isApproved] = false
            it[registrationStatus] = "REJECTED"
        } > 0
    }

    suspend fun findByEmail(email: String): User? = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun findById(id: Int): User? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun emailExists(email: String): Boolean = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .any()
    }

    suspend fun findAllUsers(): List<User> = dbQuery {
        Users.selectAll()
            .orderBy(Users.id to SortOrder.ASC)
            .map { rowToUser(it) }
    }

    private fun rowToUser(row: ResultRow) = User(
        id                 = row[Users.id],
        email              = row[Users.email],
        passwordHash       = row[Users.passwordHash],
        fullName           = row[Users.fullName],
        phone              = row[Users.phone],
        role               = row[Users.role],
        isApproved         = row[Users.isApproved],
        userType           = row[Users.userType],
        mention            = row[Users.mention],
        year               = row[Users.year],
        studentCardUrl     = row[Users.studentCardUrl],
        cin                = row[Users.cin],
        registrationStatus = row[Users.registrationStatus]
    )
}
