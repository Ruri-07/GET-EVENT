package com.getticket.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.getticket.database.*
import com.getticket.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

val JWT_SECRET: String
    get() = System.getenv("JWT_SECRET") ?: "votre_secret_tres_long_et_securise_pour_get_events"

private val dateScanFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

private fun maintenant() = dateScanFormat.format(Date())

private fun parseOrderIdFromQr(codeQR: String): Int? {
    val trimmed = codeQR.trim()
    runCatching {
        val json = Json.parseToJsonElement(trimmed).jsonObject
        json["orderId"]?.jsonPrimitive?.intOrNull?.let { return it }
    }
    Regex("""#?GET[-_]?(\d+)""", RegexOption.IGNORE_CASE)
        .find(trimmed)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
    return trimmed.toIntOrNull()
}

private fun formatEventDate(isoDate: String): String = runCatching {
    val parsed = LocalDate.parse(isoDate)
    val mois = listOf(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    )
    "${parsed.dayOfMonth} ${mois[parsed.monthValue - 1]} ${parsed.year}"
}.getOrDefault(isoDate)

private fun findActiveEvent(): ResultRow? = transaction {
    val today = LocalDate.now()
    Events.selectAll()
        .mapNotNull { row ->
            val date = runCatching { LocalDate.parse(row[Events.date]) }.getOrNull()
            row to date
        }
        .filter { (_, date) -> date == null || !date.isBefore(today) }
        .minByOrNull { (_, date) -> date ?: LocalDate.MAX }
        ?.first
        ?: Events.selectAll().orderBy(Events.id, SortOrder.DESC).limit(1).firstOrNull()
}

// ═══════════════════════════════════════════════════
// POST /api/connexion — comptes admin GET Events
// ═══════════════════════════════════════════════════
fun Route.routeConnexion() {
    post("/api/connexion") {
        val req = call.receive<ReqConnexion>()

        val user = transaction {
            Users.select { Users.email eq req.email.trim() }.firstOrNull()
        }

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized,
                RepConnexion(success = false, message = "Email introuvable"))
            return@post
        }

        if (!verifierMotDePasse(req.motDePasse, user[Users.passwordHash])) {
            call.respond(HttpStatusCode.Unauthorized,
                RepConnexion(success = false, message = "Mot de passe incorrect"))
            return@post
        }

        if (user[Users.role] != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden,
                RepConnexion(success = false, message = "Accès réservé aux administrateurs GET"))
            return@post
        }

        val token = JWT.create()
            .withSubject(user[Users.id].toString())
            .withClaim("email", user[Users.email])
            .withClaim("role", user[Users.role])
            .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000))
            .sign(Algorithm.HMAC256(JWT_SECRET))

        call.respond(RepConnexion(
            success = true,
            token = token,
            nom = user[Users.fullName],
            message = "Connexion réussie"
        ))
    }
}

// ═══════════════════════════════════════════════════
// GET /api/evenement-actif — prochain événement GET Events
// ═══════════════════════════════════════════════════
fun Route.routeEvenement() {
    authenticate("jwt") {
        get("/api/evenement-actif") {
            val ev = findActiveEvent()
            if (ev == null) {
                call.respond(HttpStatusCode.NotFound, "Aucun événement disponible")
            } else {
                call.respond(RepEvenement(
                    nom = ev[Events.title],
                    date = formatEventDate(ev[Events.date]),
                    estActif = true
                ))
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// GET /api/stats — commandes validées / utilisées
// ═══════════════════════════════════════════════════
fun Route.routeStats() {
    authenticate("jwt") {
        get("/api/stats") {
            val stats = transaction {
                val valides  = Orders.select { Orders.status eq "USED" }.count().toInt()
                val restants = Orders.select { Orders.status eq "VALIDATED" }.count().toInt()
                val refuses  = ScanHistory
                    .select { ScanHistory.status inList listOf("refuse", "doublon") }
                    .count().toInt()
                RepStats(valides, restants, refuses)
            }
            call.respond(stats)
        }
    }
}

// ═══════════════════════════════════════════════════
// GET /api/verifier/{codeQR}  +  POST /api/verifier
// ═══════════════════════════════════════════════════
fun Route.routeVerifier() {
    authenticate("jwt") {
        get("/api/verifier/{codeQR}") {
            val codeQR = call.parameters["codeQR"]?.trim() ?: run {
                call.respond(RepVerification(valide = false, message = "Code QR manquant"))
                return@get
            }
            call.respond(verifierCodeQr(codeQR))
        }
        post("/api/verifier") {
            val req = call.receive<ReqVerification>()
            call.respond(verifierCodeQr(req.codeQR.trim()))
        }
    }
}

private fun verifierCodeQr(codeQR: String): RepVerification {
    val orderId = parseOrderIdFromQr(codeQR)
    if (orderId == null) {
        transaction {
            ScanHistory.insert {
                it[ScanHistory.orderId]      = null
                it[ScanHistory.clientName]   = "Inconnu"
                it[ScanHistory.category]     = "QR invalide"
                it[ScanHistory.status]       = "refuse"
                it[ScanHistory.rejectReason] = "QR code non reconnu dans le système"
                it[ScanHistory.scannedAt]    = maintenant()
            }
        }
        return RepVerification(
            valide = false,
            message = "Ticket non reconnu",
            raisonRefus = "Ce QR code n'existe pas dans notre système"
        )
    }

    val orderRow = transaction {
        (Orders innerJoin Users)
            .select { Orders.id eq orderId }
            .firstOrNull()
    }

    if (orderRow == null) {
        transaction {
            ScanHistory.insert {
                it[ScanHistory.orderId]      = null
                it[ScanHistory.clientName]   = "Inconnu"
                it[ScanHistory.category]     = "QR invalide"
                it[ScanHistory.status]       = "refuse"
                it[ScanHistory.rejectReason] = "Commande #$orderId introuvable"
                it[ScanHistory.scannedAt]    = maintenant()
            }
        }
        return RepVerification(
            valide = false,
            message = "Ticket non reconnu",
            raisonRefus = "Aucune commande correspondante dans GET Events"
        )
    }

    val nom        = orderRow[Users.fullName]
    val userType   = orderRow[Users.userType]
    val mention    = orderRow[Users.mention]
    val year       = orderRow[Users.year]
    val promo      = listOf(mention, year).filter { it.isNotBlank() }.joinToString(" · ")
    val codeT      = "#GET-${orderId.toString().padStart(4, '0')}"
    val typeT      = when {
        orderRow[Orders.paymentMethod].equals("CONCOURS", ignoreCase = true) -> "Concours"
        userType.equals("Enseignant", ignoreCase = true) -> "Enseignant"
        else -> "Étudiant"
    }
    val orderStatus = orderRow[Orders.status]

    return when (orderStatus) {
        "PENDING" -> {
            val raison = "Paiement en attente de validation"
            transaction {
                ScanHistory.insert {
                    it[ScanHistory.orderId]      = orderId
                    it[ScanHistory.clientName]   = nom
                    it[ScanHistory.category]     = userType
                    it[ScanHistory.status]       = "refuse"
                    it[ScanHistory.rejectReason] = raison
                    it[ScanHistory.scannedAt]    = maintenant()
                }
            }
            RepVerification(
                valide = false, message = "Ticket non valide",
                nomClient = nom, categorie = userType, promotion = promo,
                codeTicket = codeT, typeTicket = typeT,
                raisonRefus = raison
            )
        }

        "CANCELLED" -> {
            val raison = "Ticket annulé par l'administrateur"
            transaction {
                ScanHistory.insert {
                    it[ScanHistory.orderId]      = orderId
                    it[ScanHistory.clientName]   = nom
                    it[ScanHistory.category]     = userType
                    it[ScanHistory.status]       = "refuse"
                    it[ScanHistory.rejectReason] = raison
                    it[ScanHistory.scannedAt]    = maintenant()
                }
            }
            RepVerification(
                valide = false, message = "Ticket annulé",
                nomClient = nom, categorie = userType, promotion = promo,
                codeTicket = codeT, typeTicket = typeT,
                raisonRefus = raison
            )
        }

        "USED" -> {
            val dateDejaUti = transaction {
                ScanHistory
                    .select { (ScanHistory.orderId eq orderId) and (ScanHistory.status eq "valide") }
                    .orderBy(ScanHistory.id, SortOrder.ASC)
                    .limit(1)
                    .firstOrNull()
                    ?.get(ScanHistory.scannedAt) ?: "date inconnue"
            }
            transaction {
                ScanHistory.insert {
                    it[ScanHistory.orderId]      = orderId
                    it[ScanHistory.clientName]   = nom
                    it[ScanHistory.category]     = userType
                    it[ScanHistory.status]       = "doublon"
                    it[ScanHistory.rejectReason] = "Déjà scanné le $dateDejaUti"
                    it[ScanHistory.scannedAt]    = maintenant()
                }
            }
            RepVerification(
                valide = false, message = "Ticket déjà utilisé",
                nomClient = nom, categorie = userType, promotion = promo,
                codeTicket = codeT, typeTicket = typeT,
                premiereUtilisation = false, dateUtilisation = dateDejaUti,
                raisonRefus = "Ticket déjà scanné le $dateDejaUti"
            )
        }

        "VALIDATED" -> {
            transaction {
                Orders.update({ Orders.id eq orderId }) {
                    it[Orders.status] = "USED"
                }
                ScanHistory.insert {
                    it[ScanHistory.orderId]      = orderId
                    it[ScanHistory.clientName]   = nom
                    it[ScanHistory.category]     = userType
                    it[ScanHistory.status]       = "valide"
                    it[ScanHistory.rejectReason] = ""
                    it[ScanHistory.scannedAt]    = maintenant()
                }
            }
            RepVerification(
                valide = true, message = "Ticket valide",
                nomClient = nom, categorie = userType, promotion = promo,
                codeTicket = codeT, typeTicket = typeT,
                premiereUtilisation = true
            )
        }

        else -> RepVerification(
            valide = false,
            message = "Statut de commande inconnu",
            raisonRefus = "Statut « $orderStatus » non géré"
        )
    }
}

// ═══════════════════════════════════════════════════
// POST /api/scan — validation et marquage ticket (nouveau endpoint)
// ═══════════════════════════════════════════════════
private fun toScanResult(rep: RepVerification): RepScanResult {
    val message = when {
        rep.valide -> "Ticket valide"
        rep.message.contains("déjà", ignoreCase = true) -> "Ticket déjà utilisé"
        rep.message.contains("non reconnu", ignoreCase = true) ||
            rep.raisonRefus.contains("introuvable", ignoreCase = true) ||
            rep.raisonRefus.contains("n'existe pas", ignoreCase = true) -> "Ticket introuvable"
        else -> rep.message
    }
    return RepScanResult(
        valid = rep.valide,
        message = message,
        nomClient = rep.nomClient,
        categorie = rep.categorie,
        promotion = rep.promotion,
        codeTicket = rep.codeTicket,
        typeTicket = rep.typeTicket,
        raisonRefus = rep.raisonRefus
    )
}

fun Route.routeScan() {
    authenticate("jwt") {
        post("/api/scan") {
            val req = call.receive<ReqScan>()
            val code = req.code.trim()
            if (code.isEmpty()) {
                call.respond(RepScanResult(valid = false, message = "Ticket introuvable"))
                return@post
            }
            call.respond(toScanResult(verifierCodeQr(code)))
        }
    }
}

// ═══════════════════════════════════════════════════
// GET /api/historique?recherche=xxx
// ═══════════════════════════════════════════════════
fun Route.routeHistorique() {
    authenticate("jwt") {
        get("/api/historique") {
            val recherche = call.request.queryParameters["recherche"] ?: ""

            val lignes = transaction {
                var q = ScanHistory.selectAll()
                if (recherche.isNotEmpty()) {
                    q = q.andWhere { ScanHistory.clientName like "%$recherche%" }
                }
                q.orderBy(ScanHistory.id, SortOrder.DESC).map { row ->
                    LigneScan(
                        nomClient        = row[ScanHistory.clientName],
                        categorie        = row[ScanHistory.category],
                        heureUtilisation = row[ScanHistory.scannedAt],
                        statut           = row[ScanHistory.status],
                        raisonRefus      = row[ScanHistory.rejectReason]
                    )
                }
            }

            call.respond(RepHistorique(
                lignes = lignes,
                stats  = RepStatsHistorique(
                    valides  = lignes.count { it.statut == "valide" },
                    refuses  = lignes.count { it.statut == "refuse" },
                    doublons = lignes.count { it.statut == "doublon" }
                )
            ))
        }
    }
}
