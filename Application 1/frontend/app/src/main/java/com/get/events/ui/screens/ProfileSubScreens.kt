package com.get.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.get.events.data.local.TokenDataStore
import com.get.events.navigation.Routes
import com.get.events.navigation.Screen
import com.get.events.ui.theme.*
import com.get.events.viewmodel.AuthViewModel
import com.get.events.viewmodel.NotificationsViewModel
import com.get.events.viewmodel.OrdersViewModel
import com.get.events.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    ordersVm: OrdersViewModel = viewModel(),
    authVm: AuthViewModel = viewModel(),
    profileVm: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("Visiteur") }
    var userEmail by remember { mutableStateOf("") }
    var isVisitor by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        TokenDataStore.getUserName(context).collect { name ->
            if (!name.isNullOrBlank()) userName = name
        }
    }
    LaunchedEffect(Unit) {
        TokenDataStore.getUserEmail(context).collect { email ->
            userEmail = email ?: ""
        }
    }
    LaunchedEffect(Unit) {
        TokenDataStore.isVisitor(context).collect { isVisitor = it }
    }
    LaunchedEffect(isVisitor) {
        if (!isVisitor) {
            ordersVm.loadOrders()
            profileVm.loadProfile()
        }
    }

    val profileUser by profileVm.user.collectAsState()
    val orderCount = ordersVm.orders.collectAsState().value.size

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Se déconnecter") },
            text = { Text("Voulez-vous vraiment vous déconnecter de votre compte ?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authVm.logout(context)
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }) { Text("Se déconnecter") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Profil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(GreenMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenDark
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(userName, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = if (isVisitor) {
                            "Mode visiteur — consultation uniquement"
                        } else if (userEmail.isNotBlank()) {
                            userEmail
                        } else {
                            "—"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    profileUser?.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (!isVisitor) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$orderCount commande(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenDark
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (isVisitor) {
                ProfileMenuItem(
                    label = "🔑  Se connecter / S'inscrire",
                    onClick = { navController.navigate(Routes.LOGIN) }
                )
            } else {
                val menuItems = listOf(
                    "👤  Mes informations" to { navController.navigate(Routes.EDIT_PROFILE) },
                    "🔔  Notifications" to { navController.navigate(Routes.NOTIFICATIONS) },
                    "🎟️  Mes tickets ($orderCount)" to { navController.navigate(Screen.MyTickets.route) },
                    "🚪  Se déconnecter" to { showLogoutDialog = true }
                )
                menuItems.forEach { (label, action) ->
                    ProfileMenuItem(label = label, onClick = action)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProfileMenuItem(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text("›", style = MaterialTheme.typography.titleLarge.copy(color = TextHint))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by vm.user.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val isSaving by vm.isSaving.collectAsState()
    val error by vm.error.collectAsState()
    val saveSuccess by vm.saveSuccess.collectAsState()

    var phone by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadProfile() }

    LaunchedEffect(user) {
        user?.let {
            phone = it.phone ?: ""
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            vm.clearSaveSuccess()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes informations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading && user == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenDark)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Vos informations d'inscription sont affichées ci-dessous. Seul le numéro de téléphone peut être complété.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        ProfileInfoRow(label = "Nom complet", value = user?.fullName ?: "—")
                        ProfileInfoRow(label = "Email", value = user?.email ?: "—")
                        ProfileInfoRow(label = "Type de compte", value = user?.userType ?: "—")
                        ProfileInfoRow(label = "Mention", value = user?.mention ?: "—")
                        if (user?.userType != "Enseignant") {
                            ProfileInfoRow(
                                label = "Année / Niveau",
                                value = user?.year?.ifBlank { "—" } ?: "—"
                            )
                        }
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Téléphone (optionnel)") },
                            placeholder = { Text("Ex : 034 12 345 67") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val current = user ?: return@Button
                        vm.updateProfile(
                            context = context,
                            fullName = current.fullName,
                            phone = phone,
                            mention = current.mention,
                            year = current.year
                        )
                    },
                    enabled = user != null && !isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Enregistrer le téléphone", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    vm: NotificationsViewModel = viewModel()
) {
    val notifications by vm.notifications.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(Unit) { vm.loadNotifications() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = { vm.markAllAsRead() }) {
                            Text("Tout lire", color = GreenDark)
                        }
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenDark)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(error!!, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
            notifications.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(
                        "Aucune notification pour le moment.\nVous serez informé lorsque vos demandes seront validées ou refusées.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    notifications.forEach { notif ->
                        NotificationCard(
                            title = notif.title,
                            message = notif.message,
                            isRead = notif.isRead,
                            onClick = { if (!notif.isRead) vm.markAsRead(notif.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    title: String,
    message: String,
    isRead: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) SurfaceWhite else GreenMint.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = if (isRead) TextPrimary else GreenDark)
            Spacer(Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(navController: NavController) {
    SimpleProfileSubScreen(
        title = "Sécurité",
        navController = navController,
        content = "Changez votre mot de passe et gérez la sécurité de votre compte."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleProfileSubScreen(
    title: String,
    navController: NavController,
    content: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}
