package com.getevents.services

import com.getevents.models.*
import com.getevents.repositories.ContestRegistrationRepository
import com.getevents.repositories.EventRepository
import com.getevents.repositories.UserRepository

class ContestService {
    private val contestRepo = ContestRegistrationRepository()
    private val eventRepo = EventRepository()
    private val userRepo = UserRepository()
    private val notificationService = NotificationService()

    suspend fun register(userId: Int, request: CreateContestRegistrationRequest): Result<Int> {
        if (request.teamName.isBlank() || request.projectTheme.isBlank()) {
            return Result.failure(Exception("Le nom d'équipe et le thème sont requis"))
        }
        if (request.membersCount < 1 || request.membersCount > 10) {
            return Result.failure(Exception("Le nombre de membres doit être entre 1 et 10"))
        }

        val event = eventRepo.findById(request.eventId)
            ?: return Result.failure(Exception("Événement non trouvé"))

        val id = contestRepo.create(
            userId       = userId,
            eventId      = request.eventId,
            teamName     = request.teamName.trim(),
            projectTheme = request.projectTheme.trim(),
            membersCount = request.membersCount
        )
        return Result.success(id)
    }

    suspend fun getAllRegistrations(): List<ContestRegistration> =
        enrich(contestRepo.findAll())

    suspend fun getPendingRegistrations(): List<ContestRegistration> =
        enrich(contestRepo.findAllPending())

    suspend fun approve(id: Int): Result<Unit> {
        val reg = contestRepo.findById(id)
            ?: return Result.failure(Exception("Inscription concours non trouvée"))
        if (reg.status != ContestRegistrationStatus.PENDING) {
            return Result.failure(Exception("Cette inscription a déjà été traitée"))
        }
        val approved = contestRepo.approve(id)
        if (approved) {
            notificationService.notify(
                userId  = reg.userId,
                title   = "Concours validé",
                message = "Votre inscription au concours « ${reg.teamName} » a été validée.",
                type    = "CONTEST_APPROVED"
            )
            return Result.success(Unit)
        }
        return Result.failure(Exception("Erreur lors de la validation"))
    }

    suspend fun reject(id: Int): Result<Unit> {
        val reg = contestRepo.findById(id)
            ?: return Result.failure(Exception("Inscription concours non trouvée"))
        if (reg.status != ContestRegistrationStatus.PENDING) {
            return Result.failure(Exception("Cette inscription a déjà été traitée"))
        }
        val rejected = contestRepo.reject(id)
        if (rejected) {
            notificationService.notify(
                userId  = reg.userId,
                title   = "Concours refusé",
                message = "Votre inscription au concours « ${reg.teamName} » a été refusée.",
                type    = "CONTEST_REJECTED"
            )
            return Result.success(Unit)
        }
        return Result.failure(Exception("Erreur lors du rejet"))
    }

    suspend fun getTeamCountForEvent(eventId: Int): Int =
        contestRepo.countByEventId(eventId)

    private suspend fun enrich(list: List<ContestRegistration>): List<ContestRegistration> =
        list.map { reg ->
            val user = userRepo.findById(reg.userId)
            val event = eventRepo.findById(reg.eventId)
            reg.copy(
                userName  = user?.fullName,
                userEmail = user?.email,
                eventTitle = event?.title
            )
        }
}
