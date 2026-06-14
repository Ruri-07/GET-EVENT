package com.get.events.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.get.events.data.api.NotificationDto
import com.get.events.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    private val _unreadCount   = MutableStateFlow(0)
    private val _isLoading     = MutableStateFlow(false)
    private val _error         = MutableStateFlow<String?>(null)

    val notifications: StateFlow<List<NotificationDto>> = _notifications
    val unreadCount:   StateFlow<Int>                   = _unreadCount
    val isLoading:     StateFlow<Boolean>               = _isLoading
    val error:         StateFlow<String?>               = _error

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _notifications.value = RetrofitClient.instance.getNotifications()
                _unreadCount.value = RetrofitClient.instance.getUnreadNotificationCount().count
            } catch (e: Exception) {
                _error.value = e.message ?: "Impossible de charger les notifications"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                _unreadCount.value = RetrofitClient.instance.getUnreadNotificationCount().count
            } catch (_: Exception) {
                _unreadCount.value = 0
            }
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.markNotificationRead(id)
                _notifications.value = _notifications.value.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
                _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
            } catch (_: Exception) { }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.markAllNotificationsRead()
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                _unreadCount.value = 0
            } catch (_: Exception) { }
        }
    }
}
