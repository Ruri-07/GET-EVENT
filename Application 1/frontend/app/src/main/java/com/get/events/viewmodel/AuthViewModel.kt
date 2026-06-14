package com.get.events.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.get.events.data.api.RegisterRequest
import com.get.events.data.api.LoginRequest
import com.get.events.data.api.ForgotPasswordRequest
import com.get.events.data.api.ResetPasswordRequest
import com.get.events.data.api.RetrofitClient
import com.get.events.data.api.UserDto
import com.get.events.data.local.TokenDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: UserDto) : AuthUiState()
    data class RegisterPending(val message: String) : AuthUiState()
    data class ForgotPasswordSent(val message: String) : AuthUiState()
    object PasswordResetSuccess : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // Données accumulées entre les 3 étapes d'inscription
    var step1Nom      = ""
    var step1Email    = ""
    var step1Password = ""
    var step2Type     = ""
    var step2Mention  = ""
    var step2Annee    = ""

    fun setStep1Data(nom: String, email: String, password: String) {
        step1Nom = nom; step1Email = email; step1Password = password
    }

    fun setStep2Data(type: String, mention: String, annee: String) {
        step2Type = type; step2Mention = mention; step2Annee = annee
    }

    fun login(email: String, password: String, context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                // Sauvegarder le token dans Retrofit + DataStore
                RetrofitClient.authToken = response.token
                TokenDataStore.saveToken(context, response.token)
                TokenDataStore.saveUserInfo(context, response.fullName, response.role, response.email)
                _uiState.value = AuthUiState.Success(
                    UserDto(
                        id       = response.userId,
                        email    = response.email,
                        fullName = response.fullName,
                        role     = response.role
                    )
                )
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Erreur de connexion. Vérifiez vos identifiants."
                )
            }
        }
    }

    fun register(
        email: String,
        password: String,
        fullName: String,
        context: Context,
        userType: String = step2Type,
        mention: String = step2Mention,
        year: String = step2Annee,
        studentCardBase64: String? = null,
        cin: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = RetrofitClient.instance.register(
                    RegisterRequest(
                        email             = email,
                        password          = password,
                        fullName          = fullName,
                        userType          = userType,
                        mention           = mention,
                        year              = year,
                        studentCardBase64 = studentCardBase64,
                        cin               = cin
                    )
                )
                if (response.pendingApproval) {
                    _uiState.value = AuthUiState.RegisterPending(response.message)
                } else {
                    _uiState.value = AuthUiState.Error("Inscription terminée mais connexion requise.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Erreur lors de la création du compte."
                )
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            TokenDataStore.clear(context)
            RetrofitClient.authToken = null
        }
        _uiState.value = AuthUiState.Idle
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = RetrofitClient.instance.forgotPassword(
                    ForgotPasswordRequest(email)
                )
                _uiState.value = AuthUiState.ForgotPasswordSent(response.message)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Impossible d'envoyer le code. Réessayez plus tard."
                )
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                RetrofitClient.instance.resetPassword(
                    ResetPasswordRequest(email, code, newPassword)
                )
                _uiState.value = AuthUiState.PasswordResetSuccess
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Code invalide ou expiré."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    // Charge le token sauvegardé au démarrage
    fun loadSavedToken(context: Context) {
        viewModelScope.launch {
            TokenDataStore.getToken(context).collect { token ->
                if (token != null) {
                    RetrofitClient.authToken = token
                }
            }
        }
    }
}
