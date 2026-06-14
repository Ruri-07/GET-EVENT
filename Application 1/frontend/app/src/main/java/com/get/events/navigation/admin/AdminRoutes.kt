package com.get.events.navigation.admin

object AdminRoutes {
    const val LOGIN              = "admin_login"
    const val DASHBOARD          = "admin_dashboard"
    const val EVENEMENTS         = "admin_evenements"
    const val PAIEMENTS          = "admin_paiements"
    const val INSCRITS           = "admin_inscrits"
    const val CONCOURS           = "admin_concours"
    const val INSCRITS_OVERVIEW  = "admin_inscrits_overview"
    const val INSCRIT_DETAIL     = "admin_inscrit_detail"
    const val VALIDATE_PAIEMENT  = "admin_validate_paiement/{paiementId}"
    const val VALIDATE_INSCRIT   = "admin_validate_inscrit/{userId}"
    fun validatePaiement(paiementId: String) = "admin_validate_paiement/$paiementId"
    fun validateInscrit(userId: String) = "admin_validate_inscrit/$userId"
    fun validateConcours(registrationId: String) = "admin_validate_concours/$registrationId"
    fun inscritDetail(userId: String) = "admin_inscrit_detail/$userId"
}
