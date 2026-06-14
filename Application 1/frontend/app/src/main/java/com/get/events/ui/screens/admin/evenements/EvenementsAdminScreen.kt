package com.get.events.ui.screens.admin.evenements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.data.model.admin.*
import com.get.events.navigation.admin.AdminRoutes
import com.get.events.ui.theme.*

/**
 * Écran Gestion des Événements (CRUD).
 *
 * Fonctionnalités :
 *  - Liste des événements avec emoji, titre, date, statut (Publié / Brouillon)
 *  - Recherche en temps réel
 *  - Bouton ✏️ Éditer et 🗑️ Supprimer par événement
 *  - FAB "+" pour ajouter un nouvel événement
 *  - Dialogs Ajouter / Éditer / Confirmer suppression
 *  - Bottom Navigation (onglet Événements actif)
 *
 * Relations BDD (via backend) :
 *  GET    /api/admin/events
 *  POST   /api/admin/events
 *  PUT    /api/admin/events/{id}
 *  DELETE /api/admin/events/{id}
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvenementsScreen(
    uiState: EvenementsUiState,
    currentRoute: String                          = AdminRoutes.EVENEMENTS,
    onNavigate: (String) -> Unit                  = {},
    onBack: () -> Unit                            = {},
    onSearchChange: (String) -> Unit              = {},
    onShowAddDialog: () -> Unit                   = {},
    onDismissAddDialog: () -> Unit                = {},
    onConfirmAdd: (EvenementFormState) -> Unit    = {},
    onShowEditDialog: (EvenementAdmin) -> Unit    = {},
    onDismissEditDialog: () -> Unit               = {},
    onConfirmEdit: (EvenementFormState) -> Unit   = {},
    onShowDeleteConfirm: (EvenementAdmin) -> Unit = {},
    onDismissDeleteConfirm: () -> Unit            = {},
    onConfirmDelete: () -> Unit                   = {},
    onActionSuccessDismissed: () -> Unit          = {},
    modifier: Modifier                            = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar sur actionSuccess
    LaunchedEffect(uiState.actionSuccess) {
        uiState.actionSuccess?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onActionSuccessDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        floatingActionButton = {
            FloatingActionButton(
                onClick          = onShowAddDialog,
                containerColor   = GreenDark.copy(alpha = 0.75f),
                contentColor     = Color.White,
                shape            = RoundedCornerShape(16.dp),
                modifier         = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = "Ajouter un événement",
                    modifier           = Modifier.size(28.dp)
                )
            }
        },
        bottomBar = {
            EvenementsBottomNav(currentRoute = currentRoute, onNavigate = onNavigate)
        },
        containerColor = BackgroundWhite,
        modifier = modifier
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Header ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundWhite)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Flèche retour
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint               = TextPrimary
                    )
                }

                // Titre centré
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text       = "Gérer les",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 26.sp,
                        color      = TextPrimary
                    )
                    Text(
                        text       = "Événements",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 26.sp,
                        color      = TextPrimary
                    )
                }
            }

            // ── Barre de recherche ───────────────────────────────────────
            OutlinedTextField(
                value         = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder   = {
                    Text(
                        text  = "Rechercher...",
                        color = TextHint
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = null,
                        tint               = TextHint
                    )
                },
                shape  = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = GreenDark,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor   = SurfaceGray,
                    unfocusedContainerColor = SurfaceGray
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── État chargement / erreur ─────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(48.dp)
                    ) {
                        CircularProgressIndicator(color = GreenDark)
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(48.dp)
                    ) {
                        Text(
                            text  = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                uiState.filteredEvenements.isEmpty() -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(48.dp)
                    ) {
                        Text(
                            text  = "Aucun événement trouvé",
                            color = TextHint
                        )
                    }
                }

                else -> {
                    // ── Liste des événements ─────────────────────────────
                    Surface(
                        color           = BackgroundWhite,
                        shape           = RoundedCornerShape(0.dp),
                        modifier        = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            uiState.filteredEvenements.forEachIndexed { index, evt ->
                                EvenementCrudItem(
                                    evenement   = evt,
                                    showDivider = index < uiState.filteredEvenements.lastIndex,
                                    onEdit      = { onShowEditDialog(evt) },
                                    onDelete    = { onShowDeleteConfirm(evt) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // espace sous le FAB
        }
    }

    // ── Dialog Ajouter ───────────────────────────────────────────────────────
    if (uiState.showAddDialog) {
        EvenementFormDialog(
            title       = "Nouvel événement",
            initialForm = EvenementFormState(),
            onDismiss   = onDismissAddDialog,
            onConfirm   = onConfirmAdd
        )
    }

    // ── Dialog Éditer ────────────────────────────────────────────────────────
    uiState.evenementToEdit?.let { evt ->
        EvenementFormDialog(
            title       = "Modifier l'événement",
            initialForm = EvenementFormState(
                titre       = evt.titre,
                dateDebut   = evt.dateDebut,
                lieu        = evt.lieu,
                emoji       = evt.emoji,
                capaciteMax = evt.capaciteMax.toString(),
                description = evt.description,
                statut       = evt.statut,
                prixEtudiant = if (evt.ticketPriceStudent > 0) evt.ticketPriceStudent.toInt().toString() else "",
                prixEnseignant = if (evt.ticketPriceTeacher > 0) evt.ticketPriceTeacher.toInt().toString() else ""
            ),
            onDismiss   = onDismissEditDialog,
            onConfirm   = onConfirmEdit
        )
    }

    // ── Dialog Confirmer suppression ─────────────────────────────────────────
    uiState.evenementToDelete?.let { evt ->
        AlertDialog(
            onDismissRequest = onDismissDeleteConfirm,
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text       = "Supprimer l'événement ?",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            },
            text = {
                Text(
                    text  = "\"${evt.titre}\" sera définitivement supprimé. Cette action est irréversible.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDelete,
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissDeleteConfirm) {
                    Text("Annuler")
                }
            }
        )
    }
}

// ── Composant : ligne événement avec actions ──────────────────────────────────

@Composable
private fun EvenementCrudItem(
    evenement: EvenementAdmin,
    showDivider: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Emoji icône
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GreenDark.copy(alpha = 0.08f))
            ) {
                Text(text = evenement.emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Titre + date + statut
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = evenement.titre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text     = evenement.dateDebut,
                        fontSize = 13.sp,
                        color    = TextSecondary
                    )
                    Text(
                        text     = " · ",
                        fontSize = 13.sp,
                        color    = TextSecondary
                    )
                    // Badge statut
                    val (badgeBg, badgeText, statutLabel) = when (evenement.statut) {
                        StatutEvenement.PUBLIE    ->
                            Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Publié")
                        StatutEvenement.BROUILLON ->
                            Triple(Color(0xFFFFF8E1), Color(0xFFF9A825), "Brouillon")
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = statutLabel,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = badgeText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Bouton Éditer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(GreenDark.copy(alpha = 0.12f))
            ) {
                IconButton(
                    onClick  = onEdit,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = "Éditer",
                        tint               = GreenDark,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Bouton Supprimer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE))
            ) {
                IconButton(
                    onClick  = onDelete,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint               = Color(0xFFD32F2F),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (showDivider) {
            Divider(
                color     = DividerColor,
                thickness = 1.dp,
                modifier  = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

// ── Dialog formulaire Ajouter / Éditer ───────────────────────────────────────

@Composable
private fun EvenementFormDialog(
    title: String,
    initialForm: EvenementFormState,
    onDismiss: () -> Unit,
    onConfirm: (EvenementFormState) -> Unit
) {
    var form by remember { mutableStateOf(initialForm) }
    val emojiOptions = listOf("🎉", "📡", "🏆", "🎓", "🎤", "🌐", "💡", "🔬")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text       = title,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sélection emoji
                Text(
                    text       = "Icône",
                    fontWeight = FontWeight.Medium,
                    fontSize   = 13.sp,
                    color      = TextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    emojiOptions.take(4).forEach { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (form.emoji == emoji) GreenDark.copy(alpha = 0.15f)
                                    else SurfaceGray
                                )
                                .clickable { form = form.copy(emoji = emoji) }
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    emojiOptions.drop(4).forEach { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (form.emoji == emoji) GreenDark.copy(alpha = 0.15f)
                                    else SurfaceGray
                                )
                                .clickable { form = form.copy(emoji = emoji) }
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                // Titre
                OutlinedTextField(
                    value         = form.titre,
                    onValueChange = { form = form.copy(titre = it) },
                    label         = { Text("Titre *") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenDark
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )

                // Date
                OutlinedTextField(
                    value         = form.dateDebut,
                    onValueChange = { form = form.copy(dateDebut = it) },
                    label         = { Text("Date (ex: 14 Juin) *") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenDark
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )

                // Lieu
                OutlinedTextField(
                    value         = form.lieu,
                    onValueChange = { form = form.copy(lieu = it) },
                    label         = { Text("Lieu *") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenDark
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )

                // Capacité
                OutlinedTextField(
                    value         = form.capaciteMax,
                    onValueChange = { form = form.copy(capaciteMax = it.filter { c -> c.isDigit() }) },
                    label         = { Text("Capacité max") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenDark
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )

                // Tarifs
                Text(
                    text       = "Tarifs des tickets",
                    fontWeight = FontWeight.Medium,
                    fontSize   = 13.sp,
                    color      = TextSecondary
                )
                OutlinedTextField(
                    value         = form.prixEtudiant,
                    onValueChange = { form = form.copy(prixEtudiant = it.filter { c -> c.isDigit() }) },
                    label         = { Text("Prix étudiant (Ar) *") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenDark
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = form.prixEnseignant,
                    onValueChange = { form = form.copy(prixEnseignant = it.filter { c -> c.isDigit() }) },
                    label         = { Text("Prix enseignant (Ar) *") },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenDark
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )

                // Statut toggle
                Text(
                    text       = "Statut",
                    fontWeight = FontWeight.Medium,
                    fontSize   = 13.sp,
                    color      = TextSecondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(StatutEvenement.BROUILLON, StatutEvenement.PUBLIE).forEach { s ->
                        val selected = form.statut == s
                        val label    = if (s == StatutEvenement.PUBLIE) "Publié" else "Brouillon"
                        FilterChip(
                            selected = selected,
                            onClick  = { form = form.copy(statut = s) },
                            label    = { Text(label, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenDark,
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    val prixValide = form.prixEtudiant.isNotBlank() && form.prixEnseignant.isNotBlank()
                    if (form.titre.isNotBlank() && form.dateDebut.isNotBlank()
                        && form.lieu.isNotBlank() && prixValide
                    ) {
                        onConfirm(form)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
            ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

// ── Bottom Navigation Bar (réutilise le même style que Dashboard) ─────────────

@Composable
private fun EvenementsBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = BackgroundWhite,
        tonalElevation = 8.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        data class NavItem(val route: String, val label: String)
        val items = listOf(
            NavItem(AdminRoutes.DASHBOARD,  "Dashboard"),
            NavItem(AdminRoutes.EVENEMENTS, "Événements"),
            NavItem(AdminRoutes.PAIEMENTS,  "Paiements"),
            NavItem(AdminRoutes.INSCRITS,   "Inscrits"),
        )
        val icons = listOf(
            Icons.Default.Dashboard,
            Icons.Default.CalendarMonth,
            Icons.Default.Payment,
            Icons.Default.People,
        )
        items.forEachIndexed { i, item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector        = icons[i],
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label, fontSize = 11.sp)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = GreenDark,
                    selectedTextColor   = GreenDark,
                    indicatorColor      = GreenDark.copy(alpha = 0.12f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EvenementsScreenPreview() {
    com.get.events.ui.theme.GetTelcoAdminTheme {
        EvenementsScreen(
            uiState = EvenementsUiState(
                evenements = listOf(
                    EvenementAdmin("e1", "Grande Réception GET 2025",  "14 Juin",    "Antananarivo", "🎉", StatutEvenement.PUBLIE,    85, 120),
                    EvenementAdmin("e2", "Semaine Télécommunications", "10–14 Juin", "ESITI",        "📡", StatutEvenement.PUBLIE,    40, 200),
                    EvenementAdmin("e3", "Concours Mini-Projet",       "20 Juin",    "Salle B",      "🏆", StatutEvenement.BROUILLON,  0,  50)
                )
            )
        )
    }
}
