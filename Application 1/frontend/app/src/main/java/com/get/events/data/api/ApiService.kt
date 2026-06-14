package com.get.events.data.api

import retrofit2.http.*

// ── Auth DTOs (alignés avec backend models/Auth.kt) ─────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

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

data class ForgotPasswordRequest(val email: String)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

data class AuthResponse(
    val token: String,
    val userId: Int,
    val email: String,
    val fullName: String,
    val role: String
)

// ── Event DTOs (alignés avec backend models/Event.kt) ───────────────────────

data class EventDto(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val ticketPriceStudent: Double,
    val ticketPriceTeacher: Double,
    val totalTickets: Int,
    val remainingTickets: Int,
    val imageUrl: String?,
    val createdBy: Int,
    val createdAt: String
)

data class CreateEventRequest(
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val ticketPriceStudent: Double,
    val ticketPriceTeacher: Double,
    val totalTickets: Int,
    val imageUrl: String? = null
)

data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val location: String? = null,
    val ticketPriceStudent: Double? = null,
    val ticketPriceTeacher: Double? = null,
    val totalTickets: Int? = null,
    val imageUrl: String? = null
)

// ── Order DTOs (alignés avec backend models/Order.kt) ───────────────────────

data class CreateOrderRequest(
    val eventId: Int,
    val quantity: Int,
    val paymentMethod: String,   // "MTN_MOMO", "ORANGE_MONEY", "MVOLA", "CONCOURS"
    val paymentReference: String? = null,
    val ticketTypeId: String? = null
)

data class OrderDto(
    val id: Int,
    val userId: Int,
    val eventId: Int,
    val eventTitle: String?,
    val quantity: Int,
    val totalAmount: Double,
    val status: String,         // "PENDING", "VALIDATED", "CANCELLED", "USED"
    val paymentMethod: String,
    val paymentReference: String?,
    val qrCodeUrl: String?,
    val createdAt: String
)

data class CreateOrderResponse(
    val orderId: Int
)

// ── User DTOs ────────────────────────────────────────────────────────────────

data class RegisterResponse(
    val message: String,
    val pendingApproval: Boolean = true
)

data class UserDto(
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

data class ContestRegistrationDto(
    val id: Int,
    val userId: Int,
    val userName: String? = null,
    val userEmail: String? = null,
    val eventId: Int,
    val eventTitle: String? = null,
    val teamName: String,
    val projectTheme: String,
    val membersCount: Int,
    val status: String,
    val createdAt: String
)

data class CreateContestRegistrationRequest(
    val eventId: Int,
    val teamName: String,
    val projectTheme: String,
    val membersCount: Int
)

data class UpdateProfileRequest(
    val fullName: String,
    val phone: String? = null,
    val mention: String? = null,
    val year: String? = null
)

data class NotificationDto(
    val id: Int,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean = false,
    val createdAt: String
)

data class ContestTeamCountResponse(
    val eventId: Int,
    val teamsCount: Int
)

data class UnreadCountResponse(val count: Int)

// ── Generic responses ────────────────────────────────────────────────────────

data class MessageResponse(val message: String)
data class IdResponse(val id: Int)

// ── API Service ───────────────────────────────────────────────────────────────

interface ApiService {

    // ── Auth ─────────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): MessageResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): MessageResponse

    // ── Events (public) ──────────────────────────────────────────────────────
    @GET("events")
    suspend fun getEvents(
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): List<EventDto>

    @GET("events/{id}")
    suspend fun getEvent(@Path("id") id: Int): EventDto

    @GET("events/{id}/contest-team-count")
    suspend fun getContestTeamCount(@Path("id") id: Int): ContestTeamCountResponse

    // ── Orders (user) ────────────────────────────────────────────────────────
    @POST("user/orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): CreateOrderResponse

    @POST("user/contest-registrations")
    suspend fun createContestRegistration(
        @Body body: CreateContestRegistrationRequest
    ): IdResponse

    @GET("user/orders")
    suspend fun getMyOrders(): List<OrderDto>

    @GET("user/orders/{orderId}")
    suspend fun getMyOrder(@Path("orderId") orderId: Int): OrderDto

    @GET("user/profile")
    suspend fun getProfile(): UserDto

    @PUT("user/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserDto

    @GET("user/notifications")
    suspend fun getNotifications(): List<NotificationDto>

    @GET("user/notifications/unread-count")
    suspend fun getUnreadNotificationCount(): UnreadCountResponse

    @PUT("user/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Int): MessageResponse

    @PUT("user/notifications/read-all")
    suspend fun markAllNotificationsRead(): MessageResponse

    // ── Admin — events CRUD ──────────────────────────────────────────────────
    @POST("admin/events")
    suspend fun createEvent(@Body body: CreateEventRequest): IdResponse

    @PUT("admin/events/{id}")
    suspend fun updateEvent(@Path("id") id: Int, @Body body: UpdateEventRequest): MessageResponse

    @DELETE("admin/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int): MessageResponse

    // ── Admin — orders validation ────────────────────────────────────────────
    @GET("admin/orders")
    suspend fun getAllOrders(): List<OrderDto>

    @PUT("admin/orders/{orderId}/validate")
    suspend fun validateOrder(@Path("orderId") orderId: Int): MessageResponse

    @PUT("admin/orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: Int): MessageResponse

    // ── Admin — users ────────────────────────────────────────────────────────
    @GET("admin/users")
    suspend fun getAllUsers(): List<UserDto>

    @PUT("admin/users/{userId}/approve")
    suspend fun approveUser(@Path("userId") userId: Int): MessageResponse

    @PUT("admin/users/{userId}/reject")
    suspend fun rejectUser(@Path("userId") userId: Int): MessageResponse

    @GET("admin/contest-registrations")
    suspend fun getContestRegistrations(): List<ContestRegistrationDto>

    @PUT("admin/contest-registrations/{id}/approve")
    suspend fun approveContestRegistration(@Path("id") id: Int): MessageResponse

    @PUT("admin/contest-registrations/{id}/reject")
    suspend fun rejectContestRegistration(@Path("id") id: Int): MessageResponse
}
