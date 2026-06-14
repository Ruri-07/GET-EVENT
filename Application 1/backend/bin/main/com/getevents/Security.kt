package com.getevents

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val jwtSecret = System.getenv("JWT_SECRET") ?: "votre_secret_tres_long_et_securise_pour_get_events"
    val jwtIssuer = "getevents-api"

    authentication {
        jwt("auth-jwt") {
            realm = "GET Events API"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.subject?.toIntOrNull()
                val email  = credential.payload.getClaim("email").asString()
                val role   = credential.payload.getClaim("role").asString() ?: "USER"

                if (userId != null) JWTPrincipal(userId, email, role) else null
            }
        }
    }
}

data class JWTPrincipal(
    val userId: Int,
    val email: String,
    val role: String
)
