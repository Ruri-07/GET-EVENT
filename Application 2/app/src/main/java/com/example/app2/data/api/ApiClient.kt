package com.getticket.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.plugins.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ─────────────────────────────────────────────────────────────
// Modèles (miroir exact du backend)
// ─────────────────────────────────────────────────────────────

@Serializable data class ReqConnexion(val email: String, val motDePasse: String)

@Serializable private data class ReqVerification(val codeQR: String)

@Serializable private data class ReqScan(val code: String)

@Serializable data class RepScanResult(
    val valid: Boolean,
    val message: String,
    val nomClient: String  = "",
    val categorie: String  = "",
    val promotion: String  = "",
    val codeTicket: String = "",
    val typeTicket: String = "",
    val raisonRefus: String = ""
)

fun RepScanResult.toRepVerification() = RepVerification(
    valide = valid,
    message = message,
    nomClient = nomClient,
    categorie = categorie,
    promotion = promotion,
    codeTicket = codeTicket,
    typeTicket = typeTicket,
    premiereUtilisation = valid,
    raisonRefus = raisonRefus.ifEmpty { if (!valid) message else "" }
)

@Serializable data class RepConnexion(
    val success: Boolean = false,
    val token: String = "",
    val nom: String = "",
    val message: String = ""
)

@Serializable private data class RepErreurServeur(val erreur: String = "")

@Serializable data class RepVerification(
    val valide: Boolean,
    val message: String,
    val nomClient: String            = "",
    val categorie: String            = "",
    val promotion: String            = "",
    val codeTicket: String           = "",
    val typeTicket: String           = "",
    val premiereUtilisation: Boolean = true,
    val dateUtilisation: String      = "",
    val raisonRefus: String          = ""
)

@Serializable data class RepEvenement(val nom: String, val date: String, val estActif: Boolean)

@Serializable data class RepStats(val valides: Int, val restants: Int, val refuses: Int)

@Serializable data class LigneScan(
    val nomClient: String, val categorie: String,
    val heureUtilisation: String, val statut: String,
    val raisonRefus: String = ""
)
@Serializable data class RepStatsHistorique(val valides: Int, val refuses: Int, val doublons: Int)
@Serializable data class RepHistorique(val lignes: List<LigneScan>, val stats: RepStatsHistorique)

// ─────────────────────────────────────────────────────────────
// Client HTTP
// ─────────────────────────────────────────────────────────────

class ApiClient {
    companion object {
        // Émulateur Android  → http://10.0.2.2:8080
        // Vrai téléphone     → http://192.168.x.x:8080  (IP du PC sur le Wi-Fi)
        const val BASE = "http://192.168.0.150:8081"
    }

    private val http = HttpClient(Android) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private fun authHeader(token: String) =
        HttpHeaders.Authorization to "Bearer $token"

    suspend fun seConnecter(email: String, mdp: String): RepConnexion = try {
        val rep = http.post("$BASE/api/connexion") {
            contentType(ContentType.Application.Json)
            setBody(ReqConnexion(email.trim(), mdp))
        }
        when (rep.status) {
            HttpStatusCode.OK, HttpStatusCode.Unauthorized -> rep.body()
            else -> {
                val msg = runCatching { rep.body<RepErreurServeur>().erreur }.getOrNull()
                    ?: "Erreur serveur (${rep.status.value})"
                RepConnexion(success = false, message = msg)
            }
        }
    } catch (e: Exception) {
        RepConnexion(success = false, message = "Erreur réseau : ${e.message}")
    }

    suspend fun scannerTicket(code: String, token: String): RepScanResult = try {
        val rep = http.post("$BASE/api/scan") {
            header(authHeader(token).first, authHeader(token).second)
            contentType(ContentType.Application.Json)
            setBody(ReqScan(code.trim()))
        }
        when (rep.status) {
            HttpStatusCode.OK -> rep.body()
            HttpStatusCode.Unauthorized -> RepScanResult(
                valid = false,
                message = "Session expirée, reconnectez-vous"
            )
            else -> {
                val msg = runCatching { rep.body<RepErreurServeur>().erreur }.getOrNull()
                    ?: "Erreur serveur (${rep.status.value})"
                RepScanResult(valid = false, message = msg)
            }
        }
    } catch (e: Exception) {
        RepScanResult(valid = false, message = "Erreur réseau : ${e.message}")
    }

    suspend fun verifierTicket(codeQR: String, token: String): RepVerification =
        scannerTicket(codeQR, token).toRepVerification()

    suspend fun chargerEvenement(token: String): RepEvenement? = try {
        val rep = http.get("$BASE/api/evenement-actif") {
            header(authHeader(token).first, authHeader(token).second)
        }
        if (rep.status == HttpStatusCode.OK) rep.body() else null
    } catch (e: Exception) { null }

    suspend fun chargerStats(token: String): RepStats? = try {
        val rep = http.get("$BASE/api/stats") {
            header(authHeader(token).first, authHeader(token).second)
        }
        if (rep.status == HttpStatusCode.OK) rep.body() else null
    } catch (e: Exception) { null }

    suspend fun chargerHistorique(token: String, recherche: String = ""): RepHistorique? = try {
        val rep = http.get("$BASE/api/historique") {
            header(authHeader(token).first, authHeader(token).second)
            if (recherche.isNotEmpty()) parameter("recherche", recherche)
        }
        if (rep.status == HttpStatusCode.OK) rep.body() else null
    } catch (e: Exception) { null }
}
