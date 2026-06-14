package com.getevents

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import kotlinx.serialization.Serializable
import io.ktor.server.http.content.*
import com.getevents.repositories.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import com.getevents.models.RegisterRequest
import com.getevents.models.RegisterResponse
import com.getevents.models.LoginRequest
import com.getevents.models.ErrorResponse
import com.getevents.services.AuthService
import com.getevents.services.EventService
import com.getevents.utils.verifyAdminRole
import com.getevents.utils.requireUserId
import com.getevents.models.CreateEventRequest
import com.getevents.models.UpdateEventRequest
import com.getevents.models.CreateOrderResponse
import com.getevents.models.MessageResponse
import com.getevents.models.IdResponse
import io.ktor.server.auth.*
import com.getevents.models.CreateOrderRequest
import com.getevents.services.OrderService
import com.getevents.services.ContestService
import com.getevents.models.ForgotPasswordRequest
import com.getevents.models.ResetPasswordRequest
import com.getevents.models.CreateContestRegistrationRequest
import com.getevents.models.UpdateProfileRequest
import com.getevents.models.ContestTeamCountResponse
import com.getevents.models.UnreadCountResponse
import com.getevents.services.NotificationService
import io.ktor.server.http.content.staticResources
import io.ktor.server.http.content.staticFiles
import java.io.File

fun Application.configureRouting() {

    // Serveur de fichiers statiques pour les QR codes et cartes étudiant
    routing {
        staticFiles("/qrcodes", File("./uploads/qrcodes"))
        staticFiles("/student-cards", File("./uploads/student-cards"))
    }
    // ===== ROUTE PUBLIC =====
    routing {
        get("/") {
            call.respondText("GET Events API — OK")
        }
        get("/health") {
            call.respond(mapOf("status" to "ok", "service" to "get-events-backend"))
        }
        get<Articles> { article ->
            // Get all articles ...
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        staticResources("/static", "static")
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        // Route de test connexion à la base de donnée
        get("/test-db") {
            try {
                val repo = UserRepository()
                val emailExists = repo.emailExists("test@test.com")
                call.respondText("MySQL connecté ! Email exists: $emailExists")
            } catch (e: Exception) {
                call.respondText("Erreur MySQL: ${e.message}")
            }
        }
        val authService = AuthService()
        post("/auth/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val result = authService.register(request)

                if (result.isSuccess) {
                    call.respond(HttpStatusCode.Created, result.getOrThrow())
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest, ErrorResponse(
                        error = "REGISTRATION_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Erreur inconnue"
                    ))
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, ErrorResponse(
                    error = "INVALID_REQUEST",
                    message = e.message ?: "Requête invalide"
                ))
            }
        }

        post("/auth/login") {
            try {
                val request = call.receive<LoginRequest>()
                val result = authService.login(request)

                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, result.getOrThrow())
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "LOGIN_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Email ou mot de passe incorrect"
                    ))
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, ErrorResponse(
                    error = "INVALID_REQUEST",
                    message = e.message ?: "Requête invalide"
                ))
            }
        }

        post("/auth/forgot-password") {
            try {
                val request = call.receive<ForgotPasswordRequest>()
                val result = authService.forgotPassword(request)
                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, result.getOrThrow())
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "FORGOT_PASSWORD_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Erreur"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    error = "INVALID_REQUEST",
                    message = e.message ?: "Requête invalide"
                ))
            }
        }

        post("/auth/reset-password") {
            try {
                val request = call.receive<ResetPasswordRequest>()
                val result = authService.resetPassword(request)
                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, result.getOrThrow())
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "RESET_PASSWORD_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Erreur"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    error = "INVALID_REQUEST",
                    message = e.message ?: "Requête invalide"
                ))
            }
        }

        val eventService = EventService()

        // Liste des événements (avec recherche et pagination)
        get("/events") {
            try {
                val search = call.request.queryParameters["search"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

                // Limiter pageSize pour éviter les abus
                val safePageSize = pageSize.coerceIn(1, 50)

                val events = eventService.getAllEvents(search, page, safePageSize)
                call.respond(events)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "SERVER_ERROR",
                    message = "Erreur lors de la récupération des événements"
                ))
            }
        }

        // Détail d'un événement
        get("/events/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID d'événement invalide"
                    ))
                    return@get
                }

                val event = eventService.getEventById(id)

                if (event == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(
                        error = "EVENT_NOT_FOUND",
                        message = "Événement non trouvé"
                    ))
                } else {
                    call.respond(event)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "SERVER_ERROR",
                    message = "Erreur lors de la récupération de l'événement"
                ))
            }
        }

        // Nombre d'équipes inscrites au concours (public)
        get("/events/{id}/contest-team-count") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID d'événement invalide"
                    ))
                    return@get
                }
                val contestService = ContestService()
                val count = contestService.getTeamCountForEvent(id)
                call.respond(ContestTeamCountResponse(eventId = id, teamsCount = count))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "SERVER_ERROR",
                    message = "Erreur lors du comptage des équipes"
                ))
            }
        }
        // ========== ROUTES ADMIN (protégées) ==========

        authenticate("auth-jwt") {

            // Créer un événement (admin uniquement)
            post("/admin/events") {
                if (!call.verifyAdminRole()) return@post

                try {
                    val request = call.receive<CreateEventRequest>()

                    // Validation basique
                    if (request.title.isBlank() || request.description.isBlank() || request.location.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "INVALID_DATA",
                            message = "Titre, description et lieu sont requis"
                        ))
                        return@post
                    }

                    if (request.ticketPriceStudent < 0 || request.ticketPriceTeacher < 0) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "INVALID_PRICE",
                            message = "Les prix ne peuvent pas être négatifs"
                        ))
                        return@post
                    }

                    if (request.totalTickets <= 0) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "INVALID_CAPACITY",
                            message = "Le nombre de tickets doit être supérieur à 0"
                        ))
                        return@post
                    }

                    val adminId = call.requireUserId() ?: return@post
                    val eventId = eventService.createEvent(request, adminId)

                    call.respond(HttpStatusCode.Created, IdResponse(id = eventId))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                        error = "CREATION_FAILED",
                        message = "Erreur lors de la création de l'événement: ${e.message}"
                    ))
                }
            }

            // Modifier un événement (admin uniquement)
            put("/admin/events/{id}") {
                if (!call.verifyAdminRole()) return@put

                try {
                    val id = call.parameters["id"]?.toIntOrNull()

                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "INVALID_ID",
                            message = "ID d'événement invalide"
                        ))
                        return@put
                    }

                    // Vérifier que l'événement existe
                    val existingEvent = eventService.getEventById(id)
                    if (existingEvent == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(
                            error = "EVENT_NOT_FOUND",
                            message = "Événement non trouvé"
                        ))
                        return@put
                    }

                    val request = call.receive<UpdateEventRequest>()
                    val updated = eventService.updateEvent(id, request)

                    if (updated) {
                        call.respond(HttpStatusCode.OK, MessageResponse(message = "Événement modifié avec succès"))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                            error = "UPDATE_FAILED",
                            message = "Erreur lors de la modification"
                        ))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                        error = "UPDATE_FAILED",
                        message = "Erreur lors de la modification: ${e.message}"
                    ))
                }
            }

            // Supprimer un événement (admin uniquement)
            delete("/admin/events/{id}") {
                if (!call.verifyAdminRole()) return@delete

                try {
                    val id = call.parameters["id"]?.toIntOrNull()

                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "INVALID_ID",
                            message = "ID d'événement invalide"
                        ))
                        return@delete
                    }

                    // Vérifier que l'événement existe
                    val existingEvent = eventService.getEventById(id)
                    if (existingEvent == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(
                            error = "EVENT_NOT_FOUND",
                            message = "Événement non trouvé"
                        ))
                        return@delete
                    }

                    val deleted = eventService.deleteEvent(id)

                    if (deleted) {
                        call.respond(HttpStatusCode.OK, MessageResponse(message = "Événement supprimé avec succès"))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                            error = "DELETE_FAILED",
                            message = "Erreur lors de la suppression"
                        ))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                        error = "DELETE_FAILED",
                        message = "Erreur lors de la suppression: ${e.message}"
                    ))
                }
            }
        }
        // ========== ROUTES UTILISATEUR (commandes) ==========

        authenticate("auth-jwt") {

            // Créer une commande (acheter des tickets)
            post("/user/orders") {
                val userId = call.requireUserId() ?: return@post

                try {
                    val request = call.receive<CreateOrderRequest>()

                    // Validation simple
                    if (request.quantity <= 0) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "INVALID_QUANTITY",
                            message = "La quantité doit être supérieure à 0"
                        ))
                        return@post
                    }

                    if (request.paymentMethod.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "INVALID_PAYMENT",
                            message = "Méthode de paiement requise"
                        ))
                        return@post
                    }

                    val orderService = OrderService()
                    val result = orderService.createOrder(userId, request)

                    if (result.isSuccess) {
                        call.respond(HttpStatusCode.Created, CreateOrderResponse(orderId = result.getOrThrow()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "ORDER_FAILED",
                            message = result.exceptionOrNull()?.message ?: "Erreur lors de la création"
                        ))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_REQUEST",
                        message = e.message ?: "Requête invalide"
                    ))
                }
            }

            // Voir toutes mes commandes
            get("/user/orders") {
                val userId = call.requireUserId() ?: return@get
                val orderService = OrderService()
                val orders = orderService.getUserOrders(userId)
                call.respond(orders)
            }

            // Inscription au concours mini-projet (sans paiement)
            post("/user/contest-registrations") {
                val userId = call.requireUserId() ?: return@post
                try {
                    val request = call.receive<CreateContestRegistrationRequest>()
                    val contestService = ContestService()
                    val result = contestService.register(userId, request)
                    if (result.isSuccess) {
                        call.respond(HttpStatusCode.Created, IdResponse(id = result.getOrThrow()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "CONTEST_REGISTRATION_FAILED",
                            message = result.exceptionOrNull()?.message ?: "Erreur"
                        ))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_REQUEST",
                        message = e.message ?: "Requête invalide"
                    ))
                }
            }

            // Profil utilisateur
            get("/user/profile") {
                val userId = call.requireUserId() ?: return@get
                val result = authService.getProfile(userId)
                if (result.isSuccess) {
                    call.respond(result.getOrThrow())
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(
                        error = "PROFILE_NOT_FOUND",
                        message = result.exceptionOrNull()?.message ?: "Profil introuvable"
                    ))
                }
            }

            put("/user/profile") {
                val userId = call.requireUserId() ?: return@put
                try {
                    val request = call.receive<UpdateProfileRequest>()
                    val result = authService.updateProfile(userId, request)
                    if (result.isSuccess) {
                        call.respond(result.getOrThrow())
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            error = "UPDATE_FAILED",
                            message = result.exceptionOrNull()?.message ?: "Erreur"
                        ))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_REQUEST",
                        message = e.message ?: "Requête invalide"
                    ))
                }
            }

            // Notifications utilisateur
            get("/user/notifications") {
                val userId = call.requireUserId() ?: return@get
                val notificationService = NotificationService()
                call.respond(notificationService.getUserNotifications(userId))
            }

            get("/user/notifications/unread-count") {
                val userId = call.requireUserId() ?: return@get
                val notificationService = NotificationService()
                call.respond(UnreadCountResponse(count = notificationService.getUnreadCount(userId)))
            }

            put("/user/notifications/{id}/read") {
                val userId = call.requireUserId() ?: return@put
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID invalide"
                    ))
                    return@put
                }
                val notificationService = NotificationService()
                val updated = notificationService.markAsRead(id, userId)
                if (updated) {
                    call.respond(MessageResponse(message = "Notification marquée comme lue"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(
                        error = "NOT_FOUND",
                        message = "Notification introuvable"
                    ))
                }
            }

            put("/user/notifications/read-all") {
                val userId = call.requireUserId() ?: return@put
                val notificationService = NotificationService()
                notificationService.markAllAsRead(userId)
                call.respond(MessageResponse(message = "Toutes les notifications ont été lues"))
            }

            // Voir une commande spécifique
            get("/user/orders/{orderId}") {
                val userId = call.requireUserId() ?: return@get
                val orderId = call.parameters["orderId"]?.toIntOrNull()

                if (orderId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID de commande invalide"
                    ))
                    return@get
                }

                val orderService = OrderService()
                val order = orderService.getUserOrder(orderId, userId)

                if (order == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(
                        error = "ORDER_NOT_FOUND",
                        message = "Commande non trouvée"
                    ))
                } else {
                    call.respond(order)
                }
            }
        }

    // ========== ROUTES ADMIN (gestion commandes) ==========

        authenticate("auth-jwt") {

            // Voir toutes les commandes
            get("/admin/orders") {
                if (!call.verifyAdminRole()) return@get

                val orderService = OrderService()
                val orders = orderService.getAllOrders()
                call.respond(orders)
            }

            // Valider une commande
            put("/admin/orders/{orderId}/validate") {
                if (!call.verifyAdminRole()) return@put

                val orderId = call.parameters["orderId"]?.toIntOrNull()

                if (orderId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID de commande invalide"
                    ))
                    return@put
                }

                val orderService = OrderService()
                val result = orderService.validateOrder(orderId)

                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, MessageResponse(message = "Commande validée avec succès"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "VALIDATION_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Erreur lors de la validation"
                    ))
                }
            }

            // Annuler une commande
            put("/admin/orders/{orderId}/cancel") {
                if (!call.verifyAdminRole()) return@put

                val orderId = call.parameters["orderId"]?.toIntOrNull()

                if (orderId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID de commande invalide"
                    ))
                    return@put
                }

                val orderService = OrderService()
                val result = orderService.cancelOrder(orderId)

                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, MessageResponse(message = "Commande annulée"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "CANCELLATION_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Erreur lors de l'annulation"
                    ))
                }
            }

            // Valider l'inscription d'un utilisateur
            put("/admin/users/{userId}/approve") {
                if (!call.verifyAdminRole()) return@put

                val userId = call.parameters["userId"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID utilisateur invalide"
                    ))
                    return@put
                }

                val authService = AuthService()
                val result = authService.approveUser(userId)

                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, MessageResponse(message = "Utilisateur validé avec succès"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "APPROVAL_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Erreur lors de la validation"
                    ))
                }
            }

            // Refuser l'inscription d'un utilisateur
            put("/admin/users/{userId}/reject") {
                if (!call.verifyAdminRole()) return@put

                val userId = call.parameters["userId"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID utilisateur invalide"
                    ))
                    return@put
                }

                val authService = AuthService()
                val result = authService.rejectUser(userId)

                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, MessageResponse(message = "Inscription refusée"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "REJECTION_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Erreur lors du rejet"
                    ))
                }
            }

            // Liste de tous les utilisateurs (admin uniquement)
            get("/admin/users") {
                if (!call.verifyAdminRole()) return@get

                val authService = AuthService()
                val users = authService.getAllUsers()
                call.respond(users)
            }

            // Inscriptions concours mini-projet en attente
            get("/admin/contest-registrations") {
                if (!call.verifyAdminRole()) return@get
                val contestService = ContestService()
                call.respond(contestService.getPendingRegistrations())
            }

            put("/admin/contest-registrations/{id}/approve") {
                if (!call.verifyAdminRole()) return@put
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID invalide"
                    ))
                    return@put
                }
                val contestService = ContestService()
                val result = contestService.approve(id)
                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, MessageResponse(message = "Inscription au concours validée"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "APPROVAL_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Erreur"
                    ))
                }
            }

            put("/admin/contest-registrations/{id}/reject") {
                if (!call.verifyAdminRole()) return@put
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "INVALID_ID",
                        message = "ID invalide"
                    ))
                    return@put
                }
                val contestService = ContestService()
                val result = contestService.reject(id)
                if (result.isSuccess) {
                    call.respond(HttpStatusCode.OK, MessageResponse(message = "Inscription au concours refusée"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "REJECTION_FAILED",
                        message = result.exceptionOrNull()?.message ?: "Erreur"
                    ))
                }
            }
        }
    }
}