package com.get.events.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.get.events.data.EventsRefreshNotifier
import com.get.events.data.api.CreateEventRequest
import com.get.events.data.api.LoginRequest
import com.get.events.data.api.OrderDto
import com.get.events.data.api.RetrofitClient
import com.get.events.data.api.UpdateEventRequest
import com.get.events.data.api.UserDto
import com.get.events.data.api.ContestRegistrationDto
import com.get.events.data.local.TokenDataStore
import com.get.events.data.model.admin.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState

    private val _dashboardState = MutableStateFlow(DashboardUiState(isLoading = true))
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState

    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users: StateFlow<List<UserDto>> = _users

    private val _orders = MutableStateFlow<List<OrderDto>>(emptyList())
    val orders: StateFlow<List<OrderDto>> = _orders

    private val _validateState = MutableStateFlow(ValidatePaiementUiState())
    val validateState: StateFlow<ValidatePaiementUiState> = _validateState

    private val _contestRegistrations = MutableStateFlow<List<ContestRegistrationDto>>(emptyList())
    val contestRegistrations: StateFlow<List<ContestRegistrationDto>> = _contestRegistrations

    private val _evenementsState = MutableStateFlow(EvenementsUiState(isLoading = true))
    val evenementsState: StateFlow<EvenementsUiState> = _evenementsState

    fun login(email: String, password: String, context: Context) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.role != TokenDataStore.ROLE_ADMIN) {
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        errorMessage = "Accès réservé aux administrateurs"
                    )
                    return@launch
                }
                RetrofitClient.authToken = response.token
                TokenDataStore.saveToken(context, response.token)
                TokenDataStore.saveUserInfo(context, response.fullName, response.role, response.email)
                _loginState.value = _loginState.value.copy(isLoading = false, isLoginSuccess = true)
            } catch (e: Exception) {
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Identifiants invalides"
                )
            }
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true, errorMessage = null)
            try {
                val orders = api.getAllOrders()
                val events = api.getEvents(pageSize = 50)
                val users = api.getAllUsers()
                val contestPending = try {
                    api.getContestRegistrations().size
                } catch (_: Exception) { 0 }
                _orders.value = orders

                val pending = orders.filter {
                    it.status.equals("PENDING", ignoreCase = true) &&
                    !it.paymentMethod.equals("CONCOURS", ignoreCase = true)
                }
                val validated = orders.count { it.status.equals("VALIDATED", ignoreCase = true) }
                val pendingUsers = users.count {
                    it.registrationStatus == "PENDING" && it.role != TokenDataStore.ROLE_ADMIN
                }

                _dashboardState.value = DashboardUiState(
                    stats = DashboardStats(
                        totalEvenements = events.size,
                        enAttente = pending.size + pendingUsers + contestPending,
                        inscrits = users.count { it.role != TokenDataStore.ROLE_ADMIN },
                        valides = validated
                    ),
                    paiementsEnAttente = pending.map { it.toPaiementEnAttente() },
                    prochainEvenements = events.take(5).map { evt ->
                        EvenementProchain(
                            id = evt.id.toString(),
                            titre = evt.title,
                            dateDebut = evt.date,
                            lieu = evt.location,
                            nbInscrits = evt.totalTickets - evt.remainingTickets,
                            capaciteMax = evt.totalTickets
                        )
                    },
                    notificationCount = pending.size + pendingUsers + contestPending,
                    isLoading = false
                )
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Erreur de chargement"
                )
            }
        }
    }

    fun loadContestRegistrations() {
        viewModelScope.launch {
            try {
                _contestRegistrations.value = api.getContestRegistrations()
            } catch (_: Exception) {
                _contestRegistrations.value = emptyList()
            }
        }
    }

    fun approveContestRegistration(id: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                api.approveContestRegistration(id)
                loadContestRegistrations()
                loadDashboard()
                onDone()
            } catch (_: Exception) { }
        }
    }

    fun rejectContestRegistration(id: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                api.rejectContestRegistration(id)
                loadContestRegistrations()
                loadDashboard()
                onDone()
            } catch (_: Exception) { }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _users.value = api.getAllUsers()
            } catch (_: Exception) {
                _users.value = emptyList()
            }
        }
    }

    fun loadOrderDetail(orderId: String) {
        viewModelScope.launch {
            _validateState.value = ValidatePaiementUiState(isLoading = true)
            try {
                val orderIdInt = orderId.toIntOrNull()
                val order = if (orderIdInt != null) {
                    _orders.value.find { it.id == orderIdInt }
                        ?: api.getAllOrders().find { it.id == orderIdInt }
                } else null

                if (order == null) {
                    _validateState.value = ValidatePaiementUiState(
                        errorMessage = "Commande introuvable"
                    )
                    return@launch
                }

                _validateState.value = ValidatePaiementUiState(
                    paiement = order.toPaiementDetail(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _validateState.value = ValidatePaiementUiState(
                    errorMessage = e.message ?: "Erreur de chargement"
                )
            }
        }
    }

    fun validateOrder(orderId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _validateState.value = _validateState.value.copy(isActionLoading = true)
            try {
                api.validateOrder(orderId.toInt())
                _validateState.value = _validateState.value.copy(
                    isActionLoading = false,
                    actionResult = ActionResult.ValidationSuccess()
                )
                loadDashboard()
                onDone()
            } catch (e: Exception) {
                _validateState.value = _validateState.value.copy(
                    isActionLoading = false,
                    actionResult = ActionResult.Error(e.message ?: "Erreur de validation")
                )
            }
        }
    }

    fun cancelOrder(orderId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _validateState.value = _validateState.value.copy(isActionLoading = true)
            try {
                api.cancelOrder(orderId.toInt())
                _validateState.value = _validateState.value.copy(
                    isActionLoading = false,
                    actionResult = ActionResult.RejetSuccess()
                )
                loadDashboard()
                onDone()
            } catch (e: Exception) {
                _validateState.value = _validateState.value.copy(
                    isActionLoading = false,
                    actionResult = ActionResult.Error(e.message ?: "Erreur de rejet")
                )
            }
        }
    }

    fun approveUser(userId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                api.approveUser(userId)
                loadUsers()
                loadDashboard()
                onDone()
            } catch (_: Exception) { }
        }
    }

    fun rejectUser(userId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                api.rejectUser(userId)
                loadUsers()
                loadDashboard()
                onDone()
            } catch (_: Exception) { }
        }
    }

    fun getUserById(userId: String): UserDto? =
        _users.value.find { it.id.toString() == userId }

    fun loadEvents() {
        viewModelScope.launch {
            _evenementsState.value = _evenementsState.value.copy(isLoading = true, errorMessage = null)
            try {
                val events = api.getEvents(pageSize = 50)
                _evenementsState.value = _evenementsState.value.copy(
                    evenements = events.map { evt ->
                        EvenementAdmin(
                            id = evt.id.toString(),
                            titre = evt.title,
                            dateDebut = evt.date,
                            lieu = evt.location,
                            emoji = emojiForTitle(evt.title),
                            statut = StatutEvenement.PUBLIE,
                            nbInscrits = evt.totalTickets - evt.remainingTickets,
                            capaciteMax = evt.totalTickets,
                            description = evt.description,
                            ticketPriceStudent = evt.ticketPriceStudent,
                            ticketPriceTeacher = evt.ticketPriceTeacher
                        )
                    },
                    isLoading = false
                )
            } catch (e: Exception) {
                _evenementsState.value = _evenementsState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Erreur de chargement"
                )
            }
        }
    }

    fun setEventsSearchQuery(query: String) {
        _evenementsState.value = _evenementsState.value.copy(searchQuery = query)
    }

    fun showAddEventDialog() {
        _evenementsState.value = _evenementsState.value.copy(showAddDialog = true)
    }

    fun dismissAddEventDialog() {
        _evenementsState.value = _evenementsState.value.copy(showAddDialog = false)
    }

    fun showEditEventDialog(evt: EvenementAdmin) {
        _evenementsState.value = _evenementsState.value.copy(evenementToEdit = evt)
    }

    fun dismissEditEventDialog() {
        _evenementsState.value = _evenementsState.value.copy(evenementToEdit = null)
    }

    fun showDeleteEventConfirm(evt: EvenementAdmin) {
        _evenementsState.value = _evenementsState.value.copy(evenementToDelete = evt)
    }

    fun dismissDeleteEventConfirm() {
        _evenementsState.value = _evenementsState.value.copy(evenementToDelete = null)
    }

    fun clearEventActionSuccess() {
        _evenementsState.value = _evenementsState.value.copy(actionSuccess = null)
    }

    fun createEvent(form: EvenementFormState) {
        viewModelScope.launch {
            try {
                api.createEvent(
                    CreateEventRequest(
                        title = form.titre.trim(),
                        description = form.description.ifBlank { form.titre },
                        date = form.dateDebut.trim(),
                        location = form.lieu.trim(),
                        ticketPriceStudent = form.prixEtudiant.replace(" ", "").toDoubleOrNull() ?: 0.0,
                        ticketPriceTeacher = form.prixEnseignant.replace(" ", "").toDoubleOrNull() ?: 0.0,
                        totalTickets = form.capaciteMax.toIntOrNull()?.coerceAtLeast(1) ?: 100
                    )
                )
                _evenementsState.value = _evenementsState.value.copy(
                    showAddDialog = false,
                    actionSuccess = "Événement créé avec succès"
                )
                EventsRefreshNotifier.notifyRefresh()
                loadEvents()
                loadDashboard()
            } catch (e: Exception) {
                _evenementsState.value = _evenementsState.value.copy(
                    errorMessage = e.message ?: "Erreur lors de la création"
                )
            }
        }
    }

    fun updateEvent(form: EvenementFormState) {
        val evt = _evenementsState.value.evenementToEdit ?: return
        val id = evt.id.toIntOrNull() ?: return
        viewModelScope.launch {
            try {
                api.updateEvent(
                    id,
                    UpdateEventRequest(
                        title = form.titre.trim(),
                        description = form.description.ifBlank { form.titre },
                        date = form.dateDebut.trim(),
                        location = form.lieu.trim(),
                        ticketPriceStudent = form.prixEtudiant.replace(" ", "").toDoubleOrNull() ?: 0.0,
                        ticketPriceTeacher = form.prixEnseignant.replace(" ", "").toDoubleOrNull() ?: 0.0,
                        totalTickets = form.capaciteMax.toIntOrNull()
                    )
                )
                _evenementsState.value = _evenementsState.value.copy(
                    evenementToEdit = null,
                    actionSuccess = "Événement modifié avec succès"
                )
                EventsRefreshNotifier.notifyRefresh()
                loadEvents()
                loadDashboard()
            } catch (e: Exception) {
                _evenementsState.value = _evenementsState.value.copy(
                    errorMessage = e.message ?: "Erreur lors de la modification"
                )
            }
        }
    }

    fun deleteEvent() {
        val evt = _evenementsState.value.evenementToDelete ?: return
        val id = evt.id.toIntOrNull() ?: return
        viewModelScope.launch {
            try {
                api.deleteEvent(id)
                _evenementsState.value = _evenementsState.value.copy(
                    evenementToDelete = null,
                    actionSuccess = "Événement supprimé"
                )
                EventsRefreshNotifier.notifyRefresh()
                loadEvents()
                loadDashboard()
            } catch (e: Exception) {
                _evenementsState.value = _evenementsState.value.copy(
                    errorMessage = e.message ?: "Erreur lors de la suppression"
                )
            }
        }
    }

    private fun emojiForTitle(title: String): String = when {
        title.contains("concours", ignoreCase = true) -> "🏆"
        title.contains("télécom", ignoreCase = true) ||
        title.contains("telecom", ignoreCase = true) -> "📡"
        title.contains("réception", ignoreCase = true) ||
        title.contains("reception", ignoreCase = true) -> "🎉"
        else -> "🎓"
    }

    fun clearActionResult() {
        _validateState.value = _validateState.value.copy(actionResult = null)
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            TokenDataStore.clear(context)
            RetrofitClient.authToken = null
            _loginState.value = LoginUiState()
        }
    }

    private fun OrderDto.toPaiementEnAttente() = PaiementEnAttente(
        id = id.toString(),
        nomClient = eventTitle ?: "Client #$userId",
        montantAr = totalAmount.toInt(),
        modePaiement = paymentMethod,
        reference = paymentReference ?: "ORDER-$id",
        statut = when (status.uppercase()) {
            "VALIDATED" -> StatutPaiement.VALIDE
            "CANCELLED" -> StatutPaiement.REJETE
            else -> StatutPaiement.EN_ATTENTE
        }
    )

    private fun OrderDto.toPaiementDetail() = PaiementDetail(
        id = id.toString(),
        nomComplet = eventTitle ?: "Utilisateur #$userId",
        typeInscrit = "Étudiant",
        filiere = "GET Télécommunications",
        telephone = getUserById(userId.toString())?.phone?.ifBlank { null }
            ?: paymentReference
            ?: "—",
        typeTicket = if (paymentMethod.equals("CONCOURS", ignoreCase = true)) {
            "Inscription concours"
        } else {
            "Ticket standard"
        },
        quantite = quantity,
        montantAr = totalAmount.toInt(),
        modePaiement = paymentMethod,
        reference = paymentReference ?: "ORDER-$id",
        statut = when (status.uppercase()) {
            "VALIDATED" -> StatutPaiement.VALIDE
            "CANCELLED" -> StatutPaiement.REJETE
            else -> StatutPaiement.EN_ATTENTE
        },
        carteEtudiantUrl = null
    )
}
