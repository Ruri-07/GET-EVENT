package com.get.events.navigation.admin

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.get.events.ui.screens.admin.dashboard.DashboardScreen
import com.get.events.ui.screens.admin.evenements.EvenementsScreen
import com.get.events.ui.screens.admin.inscrits.InscritsAdminScreen
import com.get.events.ui.screens.admin.inscrits.InscritDetailScreen
import com.get.events.ui.screens.admin.inscrits.InscritsOverviewScreen
import com.get.events.ui.screens.admin.inscrits.ValidateInscriptionScreen
import com.get.events.ui.screens.admin.concours.ConcoursAdminScreen
import com.get.events.ui.screens.admin.concours.ValidateConcoursScreen
import com.get.events.ui.screens.admin.login.LoginAdminScreen
import com.get.events.ui.screens.admin.paiement.PaiementsAdminScreen
import com.get.events.ui.screens.admin.paiement.ValidatePaiementScreen
import com.get.events.ui.theme.GetTelcoAdminTheme
import com.get.events.viewmodel.AdminViewModel

@Composable
fun AdminApp(onExitAdmin: () -> Unit = {}) {
    GetTelcoAdminTheme {
        val context = LocalContext.current
        val vm: AdminViewModel = viewModel()
        var currentScreen by remember { mutableStateOf(AdminRoutes.LOGIN) }
        var currentTab by remember { mutableStateOf(AdminRoutes.DASHBOARD) }
        var showProfileDialog by remember { mutableStateOf(false) }

        var loginEmail by remember { mutableStateOf("") }
        var loginPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        val loginState by vm.loginState.collectAsState()
        val dashboardState by vm.dashboardState.collectAsState()
        val validateState by vm.validateState.collectAsState()
        val evenementsState by vm.evenementsState.collectAsState()

        LaunchedEffect(loginState.isLoginSuccess) {
            if (loginState.isLoginSuccess) {
                currentScreen = AdminRoutes.DASHBOARD
                currentTab = AdminRoutes.DASHBOARD
                vm.loadDashboard()
            }
        }

        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                title = { Text("Profil Administrateur") },
                text = { Text("Gestion des événements, validation des tickets et des inscriptions.") },
                confirmButton = {
                    TextButton(onClick = { showProfileDialog = false }) { Text("Fermer") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showProfileDialog = false
                        vm.logout(context)
                        loginEmail = ""
                        loginPassword = ""
                        currentScreen = AdminRoutes.LOGIN
                        onExitAdmin()
                    }) { Text("Se déconnecter") }
                }
            )
        }

        when {
            currentScreen == AdminRoutes.LOGIN -> LoginAdminScreen(
                uiState = loginState.copy(
                    email = loginEmail,
                    password = loginPassword,
                    isPasswordVisible = passwordVisible
                ),
                onEmailChange = { loginEmail = it },
                onPasswordChange = { loginPassword = it },
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                onLoginClicked = { vm.login(loginEmail, loginPassword, context) },
                onNavigateToHome = {
                    currentScreen = AdminRoutes.DASHBOARD
                    vm.loadDashboard()
                }
            )

            currentScreen == AdminRoutes.EVENEMENTS -> {
                LaunchedEffect(Unit) { vm.loadEvents() }
                EvenementsScreen(
                    uiState = evenementsState,
                    currentRoute = AdminRoutes.EVENEMENTS,
                    onNavigate = { route ->
                        currentTab = route
                        currentScreen = route
                        when (route) {
                            AdminRoutes.INSCRITS -> vm.loadUsers()
                            AdminRoutes.PAIEMENTS -> vm.loadDashboard()
                            AdminRoutes.CONCOURS -> vm.loadContestRegistrations()
                            AdminRoutes.EVENEMENTS -> vm.loadEvents()
                            else -> Unit
                        }
                    },
                    onBack = { currentScreen = AdminRoutes.DASHBOARD },
                    onSearchChange = { vm.setEventsSearchQuery(it) },
                    onShowAddDialog = { vm.showAddEventDialog() },
                    onDismissAddDialog = { vm.dismissAddEventDialog() },
                    onConfirmAdd = { vm.createEvent(it) },
                    onShowEditDialog = { vm.showEditEventDialog(it) },
                    onDismissEditDialog = { vm.dismissEditEventDialog() },
                    onConfirmEdit = { vm.updateEvent(it) },
                    onShowDeleteConfirm = { vm.showDeleteEventConfirm(it) },
                    onDismissDeleteConfirm = { vm.dismissDeleteEventConfirm() },
                    onConfirmDelete = { vm.deleteEvent() },
                    onActionSuccessDismissed = { vm.clearEventActionSuccess() }
                )
            }

            currentScreen == AdminRoutes.PAIEMENTS -> PaiementsAdminScreen(
                currentRoute = AdminRoutes.PAIEMENTS,
                onNavigate = { route ->
                    currentTab = route
                    currentScreen = route
                    when (route) {
                        AdminRoutes.INSCRITS -> vm.loadUsers()
                        AdminRoutes.CONCOURS -> vm.loadContestRegistrations()
                        AdminRoutes.EVENEMENTS -> vm.loadEvents()
                        else -> Unit
                    }
                },
                onBack = { currentScreen = AdminRoutes.DASHBOARD },
                onPaiementClick = { id -> currentScreen = AdminRoutes.validatePaiement(id) },
                vm = vm
            )

            currentScreen == AdminRoutes.INSCRITS -> InscritsAdminScreen(
                currentRoute = AdminRoutes.INSCRITS,
                onNavigate = { route ->
                    currentTab = route
                    currentScreen = route
                    when (route) {
                        AdminRoutes.PAIEMENTS -> vm.loadDashboard()
                        AdminRoutes.CONCOURS -> vm.loadContestRegistrations()
                        AdminRoutes.EVENEMENTS -> vm.loadEvents()
                        else -> Unit
                    }
                },
                onBack = { currentScreen = AdminRoutes.DASHBOARD },
                onUserClick = { userId -> currentScreen = AdminRoutes.validateInscrit(userId) },
                vm = vm
            )

            currentScreen == AdminRoutes.INSCRITS_OVERVIEW -> InscritsOverviewScreen(
                onBack = { currentScreen = AdminRoutes.DASHBOARD },
                onUserClick = { userId -> currentScreen = AdminRoutes.inscritDetail(userId) },
                vm = vm
            )

            currentScreen.startsWith("admin_inscrit_detail") -> {
                val userId = currentScreen.removePrefix("admin_inscrit_detail/")
                val users by vm.users.collectAsState()
                val user = users.find { it.id.toString() == userId }
                LaunchedEffect(userId) { vm.loadUsers() }
                if (user != null) {
                    InscritDetailScreen(
                        user = user,
                        onBack = { currentScreen = AdminRoutes.INSCRITS_OVERVIEW }
                    )
                }
            }

            currentScreen == AdminRoutes.CONCOURS -> ConcoursAdminScreen(
                currentRoute = AdminRoutes.CONCOURS,
                onNavigate = { route ->
                    currentTab = route
                    currentScreen = route
                    when (route) {
                        AdminRoutes.INSCRITS -> vm.loadUsers()
                        AdminRoutes.PAIEMENTS -> vm.loadDashboard()
                        AdminRoutes.EVENEMENTS -> vm.loadEvents()
                        else -> Unit
                    }
                },
                onBack = { currentScreen = AdminRoutes.DASHBOARD },
                onRegistrationClick = { id -> currentScreen = AdminRoutes.validateConcours(id) },
                vm = vm
            )

            currentScreen.startsWith("admin_validate_concours") -> {
                val regId = currentScreen.removePrefix("admin_validate_concours/")
                val registrations by vm.contestRegistrations.collectAsState()
                val registration = registrations.find { it.id.toString() == regId }
                LaunchedEffect(regId) { vm.loadContestRegistrations() }
                if (registration != null) {
                    ValidateConcoursScreen(
                        registration = registration,
                        onBack = { currentScreen = AdminRoutes.CONCOURS },
                        onApprove = { currentScreen = AdminRoutes.CONCOURS },
                        onReject = { currentScreen = AdminRoutes.CONCOURS },
                        vm = vm
                    )
                }
            }

            currentScreen.startsWith("admin_validate_inscrit") -> {
                val userId = currentScreen.removePrefix("admin_validate_inscrit/")
                val users by vm.users.collectAsState()
                val user = users.find { it.id.toString() == userId }
                LaunchedEffect(userId) { vm.loadUsers() }
                if (user != null) {
                    ValidateInscriptionScreen(
                        user = user,
                        onBack = { currentScreen = AdminRoutes.INSCRITS },
                        onApprove = { currentScreen = AdminRoutes.INSCRITS },
                        onReject = { currentScreen = AdminRoutes.INSCRITS },
                        vm = vm
                    )
                }
            }

            currentScreen.startsWith("admin_validate_paiement") -> {
                val paiementId = currentScreen.removePrefix("admin_validate_paiement/")
                LaunchedEffect(paiementId) { vm.loadOrderDetail(paiementId) }
                ValidatePaiementScreen(
                    uiState = validateState,
                    onBack = { currentScreen = AdminRoutes.DASHBOARD },
                    onValider = { vm.validateOrder(paiementId) { currentScreen = AdminRoutes.DASHBOARD } },
                    onRejeter = { vm.cancelOrder(paiementId) { currentScreen = AdminRoutes.DASHBOARD } },
                    onActionDismissed = { vm.clearActionResult() }
                )
            }

            else -> DashboardScreen(
                uiState = dashboardState,
                currentRoute = currentTab,
                onNavigate = { route ->
                    currentTab = route
                    currentScreen = route
                    when (route) {
                        AdminRoutes.INSCRITS -> vm.loadUsers()
                        AdminRoutes.PAIEMENTS -> vm.loadDashboard()
                        AdminRoutes.CONCOURS -> vm.loadContestRegistrations()
                        AdminRoutes.EVENEMENTS -> vm.loadEvents()
                        else -> vm.loadDashboard()
                    }
                },
                onRefresh = { vm.loadDashboard() },
                onPaiementClick = { id -> currentScreen = AdminRoutes.validatePaiement(id) },
                onNotificationsClick = { currentScreen = AdminRoutes.PAIEMENTS },
                onProfileClick = { showProfileDialog = true },
                onInscritsClick = {
                    vm.loadUsers()
                    currentScreen = AdminRoutes.INSCRITS_OVERVIEW
                }
            )
        }
    }
}
