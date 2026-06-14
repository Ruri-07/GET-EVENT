package com.getevents.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object Jwt {
    private val SECRET    = System.getenv("JWT_SECRET") ?: "votre_secret_tres_long_et_securise_pour_get_events"
    private const val ISSUER      = "getevents-api"
    private const val EXPIRES_MS  = 86400000L // 24h

    fun generateToken(userId: Int, email: String, role: String): String {
        return JWT.create()
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withClaim("role", role)
            .withIssuer(ISSUER)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRES_MS))
            .sign(Algorithm.HMAC256(SECRET))
    }
}
