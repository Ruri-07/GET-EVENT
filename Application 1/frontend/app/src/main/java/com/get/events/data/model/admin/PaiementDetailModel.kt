package com.get.events.data.model.admin

/**
 * Détail complet d'un paiement chargé depuis la base de données.
 *
 * Source : GET /api/admin/payments/{id}
 *
 * Ce modèle enrichit [PaiementEnAttente] avec les informations complètes
 * de l'inscrit (nom, filière, ticket, contact) nécessaires pour l'écran
 * de validation / rejet.
 */
data class PaiementDetail(
    val id: String,

    // ── Informations de l'inscrit (table Inscrit en BDD) ─────────────────
    val nomComplet: String,
    val typeInscrit: String,        // ex: "Étudiant", "Professionnel", "VIP"
    val filiere: String,            // ex: "L3 Télécommunications"
    val telephone: String,          // ex: "034 87 654 32"

    // ── Informations du ticket (table Ticket en BDD) ──────────────────────
    val typeTicket: String,         // ex: "Ticket Étudiant"
    val quantite: Int,              // ex: 1
    val montantAr: Int,             // ex: 5000

    // ── Informations du paiement (table Paiement en BDD) ─────────────────
    val modePaiement: String,       // ex: "MVola", "Orange Money"
    val reference: String,          // ex: "TXN-12345678"
    val statut: StatutPaiement,

    // ── Pièce justificative (table Document en BDD) ───────────────────────
    val carteEtudiantUrl: String?,  // URL de la photo carte étudiant (nullable)
    val hasCarteEtudiante: Boolean  = carteEtudiantUrl != null,

    // ── Métadonnées ───────────────────────────────────────────────────────
    val dateCreation: String        = "",   // ISO 8601
    val evenementId: String         = ""
)

/**
 * État UI de l'écran Valider Paiement.
 */
data class ValidatePaiementUiState(
    val paiement: PaiementDetail?       = null,
    val isLoading: Boolean              = false,
    val isActionLoading: Boolean        = false,  // loading spécifique au bouton Valider/Rejeter
    val errorMessage: String?           = null,
    val actionResult: ActionResult?     = null
)

/**
 * Résultat d'une action de validation ou rejet.
 * Affiché dans un dialog ou snackbar après l'action.
 */
sealed class ActionResult {
    data class ValidationSuccess(val message: String = "Ticket QR envoyé à l'étudiant") : ActionResult()
    data class RejetSuccess(val message: String = "Paiement rejeté") : ActionResult()
    data class Error(val message: String) : ActionResult()
}
