package com.get.events.data.model.admin

/**
 * Statistiques globales affichées dans les 4 cartes du Dashboard.
 * Ces données sont chargées via DashboardRepository → GET /api/admin/dashboard/stats
 */
data class DashboardStats(
    val totalEvenements: Int = 0,
    val enAttente: Int       = 0,
    val inscrits: Int        = 0,
    val valides: Int         = 0
)

/**
 * Représente un paiement en attente de validation.
 * Source : GET /api/admin/payments?status=PENDING
 */
data class PaiementEnAttente(
    val id: String,
    val nomClient: String,
    val montantAr: Int,
    val modePaiement: String,   // "MVola", "Orange Money", "Airtel Money", etc.
    val reference: String,      // ex: "TXN-12345678"
    val statut: StatutPaiement  = StatutPaiement.EN_ATTENTE
)

enum class StatutPaiement {
    EN_ATTENTE, VALIDE, REJETE
}

/**
 * Représente un événement à venir.
 * Source : GET /api/admin/events?upcoming=true
 */
data class EvenementProchain(
    val id: String,
    val titre: String,
    val dateDebut: String,      // ISO 8601, affiché formaté dans l'UI
    val lieu: String,
    val nbInscrits: Int,
    val capaciteMax: Int
)

/**
 * État global de l'UI du Dashboard.
 */
data class DashboardUiState(
    val stats: DashboardStats               = DashboardStats(),
    val paiementsEnAttente: List<PaiementEnAttente> = emptyList(),
    val prochainEvenements: List<EvenementProchain> = emptyList(),
    val isLoading: Boolean                  = false,
    val errorMessage: String?               = null,
    val adminName: String                   = "Admin",
    val notificationCount: Int              = 0
)
