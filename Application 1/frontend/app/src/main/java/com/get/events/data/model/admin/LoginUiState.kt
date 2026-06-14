package com.get.events.data.model.admin

/**
 * Représente l'état de l'UI pour la page de connexion Admin.
 * Aucun appel réseau ici — le ViewModel gère la liaison avec le Repository.
 */
data class LoginUiState(
    val email: String            = "",
    val password: String         = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean       = false,
    val errorMessage: String?    = null,
    val isLoginSuccess: Boolean  = false
)
