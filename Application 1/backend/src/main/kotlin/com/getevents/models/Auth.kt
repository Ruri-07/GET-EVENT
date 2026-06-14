package com.getevents.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String? = null,
    val userType: String = "Étudiant",
    val mention: String = "Télécommunications",
    val year: String = "",
    val studentCardBase64: String? = null,
    val cin: String? = null
)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Int,
    val email: String,
    val fullName: String,
    val role: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

@Serializable
data class UserPublic(
    val id: Int,
    val email: String,
    val fullName: String,
    val phone: String? = null,
    val role: String,
    val isApproved: Boolean = false,
    val userType: String = "Étudiant",
    val mention: String = "Télécommunications",
    val year: String = "",
    val studentCardUrl: String? = null,
    val cin: String? = null,
    val registrationStatus: String = "PENDING"
)

@Serializable
data class RegisterResponse(
    val message: String,
    val pendingApproval: Boolean = true
)

@Serializable
data class CreateOrderResponse(val orderId: Int)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class IdResponse(val id: Int)

@Serializable
data class UpdateProfileRequest(
    val fullName: String,
    val phone: String? = null,
    val mention: String? = null,
    val year: String? = null
)

@Serializable
data class ContestTeamCountResponse(val eventId: Int, val teamsCount: Int)

@Serializable
data class UnreadCountResponse(val count: Int)
