package com.getevents.services

import com.getevents.models.*
import com.getevents.repositories.UserRepository
import com.getevents.repositories.PasswordResetRepository
import com.getevents.utils.StudentCardStorage
import com.getevents.utils.EmailService
import com.getevents.utils.hashPassword
import com.getevents.utils.verifyPassword
import com.getevents.utils.Jwt
import kotlin.random.Random

class AuthService {
    private val userRepo = UserRepository()
    private val resetRepo = PasswordResetRepository()
    private val notificationService = NotificationService()

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        if (userRepo.emailExists(request.email)) {
            return Result.failure(Exception("Cet email est déjà utilisé"))
        }

        val hashedPassword = hashPassword(request.password)

        val userId = userRepo.create(
            email              = request.email,
            passwordHash       = hashedPassword,
            fullName           = request.fullName,
            phone              = request.phone,
            role               = "USER",
            isApproved         = false,
            userType           = request.userType,
            mention            = request.mention,
            year               = request.year,
            cin                = request.cin,
            registrationStatus = "PENDING"
        )

        request.studentCardBase64?.let { base64 ->
            StudentCardStorage.saveFromBase64(base64, userId)?.let { url ->
                userRepo.updateStudentCardUrl(userId, url)
            }
        }

        return Result.success(
            RegisterResponse(
                message = "Inscription enregistrée ! Votre compte sera activé après vérification par l'administrateur.",
                pendingApproval = true
            )
        )
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        val user = userRepo.findByEmail(request.email)
            ?: return Result.failure(Exception("Email ou mot de passe incorrect"))

        if (!verifyPassword(request.password, user.passwordHash)) {
            return Result.failure(Exception("Email ou mot de passe incorrect"))
        }

        if (user.role != "ADMIN") {
            when (user.registrationStatus) {
                "REJECTED" -> return Result.failure(
                    Exception("Votre inscription a été refusée. Contactez l'administration GET.")
                )
                "PENDING" -> return Result.failure(
                    Exception("Votre compte est en attente de validation par l'administrateur")
                )
            }
            if (!user.isApproved) {
                return Result.failure(
                    Exception("Votre compte est en attente de validation par l'administrateur")
                )
            }
        }

        val token = Jwt.generateToken(user.id, user.email, user.role)

        return Result.success(
            AuthResponse(
                token    = token,
                userId   = user.id,
                email    = user.email,
                fullName = user.fullName,
                role     = user.role
            )
        )
    }

    suspend fun approveUser(userId: Int): Result<Unit> {
        val user = userRepo.findById(userId)
            ?: return Result.failure(Exception("Utilisateur non trouvé"))
        if (user.role == "ADMIN") {
            return Result.failure(Exception("Impossible de modifier un administrateur"))
        }
        if (user.registrationStatus == "APPROVED") {
            return Result.failure(Exception("Utilisateur déjà validé"))
        }
        val updated = userRepo.approveUser(userId)
        if (updated) {
            notificationService.notify(
                userId  = userId,
                title   = "Inscription validée",
                message = "Votre compte a été validé par l'administration GET. Vous pouvez maintenant vous connecter.",
                type    = "REGISTRATION_APPROVED"
            )
            return Result.success(Unit)
        }
        return Result.failure(Exception("Erreur lors de la validation"))
    }

    suspend fun rejectUser(userId: Int): Result<Unit> {
        val user = userRepo.findById(userId)
            ?: return Result.failure(Exception("Utilisateur non trouvé"))
        if (user.role == "ADMIN") {
            return Result.failure(Exception("Impossible de modifier un administrateur"))
        }
        val updated = userRepo.rejectUser(userId)
        if (updated) {
            notificationService.notify(
                userId  = userId,
                title   = "Inscription refusée",
                message = "Votre demande d'inscription a été refusée. Contactez l'administration GET pour plus d'informations.",
                type    = "REGISTRATION_REJECTED"
            )
            return Result.success(Unit)
        }
        return Result.failure(Exception("Erreur lors du rejet"))
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): Result<MessageResponse> {
        val email = request.email.trim()
        if (email.isBlank()) {
            return Result.failure(Exception("Email requis"))
        }

        val user = userRepo.findByEmail(email)
        // Ne pas révéler si l'email existe (sécurité)
        if (user != null) {
            val code = Random.nextInt(100000, 999999).toString()
            val expiresAt = System.currentTimeMillis() + 15 * 60 * 1000
            resetRepo.createToken(email, code, expiresAt)
            EmailService.sendPasswordResetCode(email, code, user.fullName)
        }

        return Result.success(
            MessageResponse(
                message = "Si cet email est enregistré, un code de vérification vous a été envoyé."
            )
        )
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Result<MessageResponse> {
        val email = request.email.trim()
        if (email.isBlank() || request.code.isBlank()) {
            return Result.failure(Exception("Email et code requis"))
        }
        if (request.newPassword.length < 6) {
            return Result.failure(Exception("Le mot de passe doit contenir au moins 6 caractères"))
        }

        val valid = resetRepo.findValidToken(email, request.code.trim())
        if (!valid) {
            return Result.failure(Exception("Code invalide ou expiré"))
        }

        val user = userRepo.findByEmail(email)
            ?: return Result.failure(Exception("Utilisateur non trouvé"))

        val hashed = hashPassword(request.newPassword)
        userRepo.updatePassword(user.id, hashed)
        resetRepo.markUsed(email, request.code.trim())

        return Result.success(
            MessageResponse(message = "Mot de passe réinitialisé avec succès. Vous pouvez vous connecter.")
        )
    }

    suspend fun getAllUsers(): List<UserPublic> =
        userRepo.findAllUsers().map { user -> user.toPublic() }

    suspend fun getProfile(userId: Int): Result<UserPublic> {
        val user = userRepo.findById(userId)
            ?: return Result.failure(Exception("Utilisateur non trouvé"))
        return Result.success(user.toPublic())
    }

    suspend fun updateProfile(userId: Int, request: UpdateProfileRequest): Result<UserPublic> {
        if (request.fullName.isBlank()) {
            return Result.failure(Exception("Le nom complet est requis"))
        }
        val updated = userRepo.updateProfile(
            id       = userId,
            fullName = request.fullName.trim(),
            phone    = request.phone?.trim(),
            mention  = request.mention?.trim(),
            year     = request.year?.trim()
        )
        if (!updated) {
            return Result.failure(Exception("Erreur lors de la mise à jour du profil"))
        }
        return getProfile(userId)
    }

    private fun User.toPublic() = UserPublic(
        id                 = id,
        email              = email,
        fullName           = fullName,
        phone              = phone,
        role               = role,
        isApproved         = isApproved,
        userType           = userType,
        mention            = mention,
        year               = year,
        studentCardUrl     = studentCardUrl,
        cin                = cin,
        registrationStatus = registrationStatus
    )
}
