package com.getevents.services

import com.getevents.models.NotificationDto
import com.getevents.repositories.NotificationRepository

class NotificationService {
    private val repo = NotificationRepository()

    suspend fun notify(userId: Int, title: String, message: String, type: String) {
        repo.create(userId, title, message, type)
    }

    suspend fun getUserNotifications(userId: Int): List<NotificationDto> =
        repo.findByUserId(userId)

    suspend fun getUnreadCount(userId: Int): Int =
        repo.countUnread(userId)

    suspend fun markAsRead(id: Int, userId: Int): Boolean =
        repo.markAsRead(id, userId)

    suspend fun markAllAsRead(userId: Int) {
        repo.markAllAsRead(userId)
    }
}
