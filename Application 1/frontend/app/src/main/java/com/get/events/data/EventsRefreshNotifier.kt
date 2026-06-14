package com.get.events.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Signale aux écrans utilisateur qu'ils doivent recharger la liste des événements
 * (ex. après création/modification côté admin ou retour sur l'onglet Accueil).
 */
object EventsRefreshNotifier {
    private val _trigger = MutableStateFlow(0)
    val trigger: StateFlow<Int> = _trigger.asStateFlow()

    fun notifyRefresh() {
        _trigger.value++
    }
}
