package com.getevents.utils

import com.getevents.JWTPrincipal
import com.getevents.models.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authentication
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

suspend fun ApplicationCall.verifyAdminRole(): Boolean {
    val principal = authentication.principal<JWTPrincipal>()

    return if (principal?.role != "ADMIN") {
        respond(HttpStatusCode.Forbidden, ErrorResponse(
            error   = "ACCESS_DENIED",
            message = "Accès réservé aux administrateurs"
        ))
        false
    } else {
        true
    }
}

suspend fun ApplicationCall.requireUserId(): Int? {
    val userId = authentication.principal<JWTPrincipal>()?.userId
    if (userId == null) {
        respond(
            HttpStatusCode.Unauthorized,
            com.getevents.models.ErrorResponse(
                error = "UNAUTHORIZED",
                message = "Authentification requise"
            )
        )
        return null
    }
    return userId
}

suspend fun ApplicationCall.getUserId(): Int =
    authentication.principal<JWTPrincipal>()?.userId ?: 0

suspend fun ApplicationCall.getUserRole(): String {
    return authentication.principal<JWTPrincipal>()?.role ?: "USER"
}
