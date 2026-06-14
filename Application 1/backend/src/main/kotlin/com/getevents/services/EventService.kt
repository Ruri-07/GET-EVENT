package com.getevents.services

import com.getevents.models.*
import com.getevents.repositories.EventRepository

class EventService {
    private val eventRepo = EventRepository()

    // Récupérer tous les événements (avec filtres)
    suspend fun getAllEvents(search: String? = null, page: Int = 1, pageSize: Int = 10): List<Event> {
        return eventRepo.findAll(search, page, pageSize)
    }

    // Récupérer un événement par son ID
    suspend fun getEventById(id: Int): Event? {
        return eventRepo.findById(id)
    }

    // Créer un événement (admin)
    suspend fun createEvent(request: CreateEventRequest, adminId: Int): Int {
        return eventRepo.create(request, adminId)
    }

    // Modifier un événement (admin)
    suspend fun updateEvent(id: Int, request: UpdateEventRequest): Boolean {
        return eventRepo.update(id, request)
    }

    // Supprimer un événement (admin)
    suspend fun deleteEvent(id: Int): Boolean {
        return eventRepo.delete(id)
    }
}