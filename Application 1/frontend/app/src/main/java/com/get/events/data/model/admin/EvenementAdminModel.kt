package com.get.events.data.model.admin

/**
 * Représente un événement complet pour la gestion CRUD.
 *
 * Source : GET /api/admin/events
 */
data class EvenementAdmin(
    val id: String,
    val titre: String,
    val dateDebut: String,          // ex: "14 Juin" ou "10–14 Juin"
    val lieu: String,
    val emoji: String,              // ex: "🎉", "📡", "🏆"
    val statut: StatutEvenement,
    val nbInscrits: Int     = 0,
    val capaciteMax: Int    = 0,
    val description: String = "",
    val ticketPriceStudent: Double = 0.0,
    val ticketPriceTeacher: Double = 0.0
)

enum class StatutEvenement {
    PUBLIE, BROUILLON
}

/**
 * État UI de l'écran Gestion Événements.
 */
data class EvenementsUiState(
    val evenements: List<EvenementAdmin>  = emptyList(),
    val isLoading: Boolean                = false,
    val errorMessage: String?             = null,
    val searchQuery: String               = "",
    val evenementToDelete: EvenementAdmin? = null,    // dialog confirmation suppression
    val evenementToEdit: EvenementAdmin?   = null,    // dialog édition
    val showAddDialog: Boolean             = false,   // dialog ajout
    val actionSuccess: String?             = null     // snackbar message
) {
    /** Liste filtrée selon la recherche */
    val filteredEvenements: List<EvenementAdmin>
        get() = if (searchQuery.isBlank()) evenements
                else evenements.filter {
                    it.titre.contains(searchQuery, ignoreCase = true) ||
                    it.lieu.contains(searchQuery, ignoreCase = true)
                }
}

/**
 * Données saisies dans le formulaire Ajouter / Éditer.
 */
data class EvenementFormState(
    val titre: String       = "",
    val dateDebut: String   = "",
    val lieu: String        = "",
    val emoji: String       = "🎉",
    val capaciteMax: String = "",
    val description: String = "",
    val statut: StatutEvenement = StatutEvenement.BROUILLON,
    val prixEtudiant: String  = "",
    val prixEnseignant: String = ""
)
