package com.get.events.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.get.events.data.EventsRefreshNotifier
import com.get.events.data.model.Event
import com.get.events.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EventsUiState {
    object Loading : EventsUiState()
    data class Ready(
        val featured: List<Event>,
        val upcoming: List<Event>
    ) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}

class EventsViewModel : ViewModel() {

    private val repository = EventRepository()

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent

    init {
        loadEvents()
        viewModelScope.launch {
            EventsRefreshNotifier.trigger.collect {
                loadEvents()
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            try {
                val all = repository.getAllEvents()
                val featured = all.map { it.copy(isFeatured = true) }
                _uiState.value = EventsUiState.Ready(
                    featured = featured,
                    upcoming = all
                )
            } catch (e: Exception) {
                _uiState.value = EventsUiState.Error(
                    e.message ?: "Impossible de charger les événements"
                )
            }
        }
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            repository.getEventById(eventId).collect { event ->
                _selectedEvent.value = event
            }
        }
    }

    fun searchEvents(query: String, onResult: (List<Event>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.searchEvents(query))
        }
    }
}
