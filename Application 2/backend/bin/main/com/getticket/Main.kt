package com.getticket

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.getticket.database.connecterBDD
import com.getticket.models.RepConnexion
import com.getticket.routes.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    connecterBDD()
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8081
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        configurerApp()
    }.start(wait = true)
}

fun Application.configurerApp() {

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                RepConnexion(success = false, message = cause.message ?: "Erreur interne")
            )
        }
    }

    install(Authentication) {
        jwt("jwt") {
            verifier(JWT.require(Algorithm.HMAC256(JWT_SECRET)).build())
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token invalide ou expiré")
            }
        }
    }

    routing {
        routeConnexion()    // POST /api/connexion
        routeEvenement()    // GET  /api/evenement-actif
        routeStats()        // GET  /api/stats
        routeVerifier()     // GET/POST /api/verifier
        routeScan()         // POST /api/scan
        routeHistorique()   // GET  /api/historique
    }
}
