package com.get.events.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.get.events.data.api.RetrofitClient
import com.get.events.data.api.UpdateProfileRequest
import com.get.events.data.api.UserDto
import com.get.events.data.local.TokenDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _user       = MutableStateFlow<UserDto?>(null)
    private val _isLoading  = MutableStateFlow(false)
    private val _isSaving   = MutableStateFlow(false)
    private val _error      = MutableStateFlow<String?>(null)
    private val _saveSuccess = MutableStateFlow(false)

    val user:        StateFlow<UserDto?>   = _user
    val isLoading:   StateFlow<Boolean>    = _isLoading
    val isSaving:    StateFlow<Boolean>    = _isSaving
    val error:       StateFlow<String?>    = _error
    val saveSuccess: StateFlow<Boolean>    = _saveSuccess

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _user.value = RetrofitClient.instance.getProfile()
            } catch (e: Exception) {
                _error.value = e.message ?: "Impossible de charger le profil"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(
        context: Context,
        fullName: String,
        phone: String,
        mention: String,
        year: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            _saveSuccess.value = false
            try {
                val updated = RetrofitClient.instance.updateProfile(
                    UpdateProfileRequest(
                        fullName = fullName.trim(),
                        phone    = phone.trim().ifBlank { null },
                        mention  = mention.trim().ifBlank { null },
                        year     = year.trim().ifBlank { null }
                    )
                )
                _user.value = updated
                TokenDataStore.saveUserInfo(context, updated.fullName, updated.role, updated.email)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la mise à jour"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }
}
