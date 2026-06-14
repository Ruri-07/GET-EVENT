package com.get.events.ui.screens.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.data.model.admin.*
import com.get.events.navigation.admin.AdminRoutes
import com.get.events.ui.components.admin.*
import com.get.events.ui.theme.*

/**
 * Écran Dashboard Admin GET Telco.
 *
 * Architecture stateless :
 *  - Reçoit [uiState] depuis le DashboardViewModel
 *  - Remonte les événements (refresh, actions) via lambdas
 *  - La Bottom Navigation est gérée ici (4 onglets : Dashboard, Événements, Paiements, Inscrits)
 *
 * Relation backend :
 *  - Les stats sont chargées via DashboardRepository.getStats()
 *  - Les paiements via DashboardRepository.getPaiementsEnAttente()
 *  - Les événements via DashboardRepository.getProchainEvenements()
 */
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    currentRoute: String             = AdminRoutes.DASHBOARD,
    onNavigate: (String) -> Unit     = {},
    onRefresh: () -> Unit               = {},
    onValiderPaiement: (String) -> Unit = {},
    onRejeterPaiement: (String) -> Unit = {},
    onPaiementClick: (String) -> Unit   = {},   // navigue vers ValidatePaiementScreen
    onNotificationsClick: () -> Unit   = {},
    onProfileClick: () -> Unit         = {},
    onInscritsClick: () -> Unit        = {},
    modifier: Modifier               = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        bottomBar = {
            Surface(shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                com.get.events.ui.components.admin.AdminBottomNavigationBarShared(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate
                )
            }
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

            // ── SECTION HAUTE : fond vert ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(GreenDark, GreenMedium)
                        )
                    )
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp)
            ) {
                Column {
                    // Titre + badge ADMIN + cloche
                    Row(
                        verticalAlignment     = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text       = "Dashboard\nAdmin",
                            color      = TextOnGreen,
                            fontSize   = 26.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Badge ADMIN + profil
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable { onProfileClick() }
                            ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .background(
                                        color = GreenLight.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint               = TextOnGreen,
                                    modifier           = Modifier.size(14.dp)
                                )
                                Text(
                                    text       = "ADMIN",
                                    color      = TextOnGreen,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profil admin",
                                tint = TextOnGreen.copy(alpha = 0.85f),
                                modifier = Modifier.size(28.dp)
                            )
                            }

                            // Icône cloche notification
                            BadgedBox(
                                modifier = Modifier.clickable { onNotificationsClick() },
                                badge = {
                                    if (uiState.notificationCount > 0) {
                                        Badge(
                                            containerColor = GoldPrimary
                                        ) {
                                            Text(
                                                text  = uiState.notificationCount.toString(),
                                                color = Color.White,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint               = TextOnGreen.copy(alpha = 0.85f),
                                    modifier           = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Grille 2×2 des statistiques
                    if (uiState.isLoading) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            CircularProgressIndicator(color = TextOnGreen)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatCard(
                                    value  = uiState.stats.totalEvenements.toString(),
                                    label  = "ÉVÉNEMENTS",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    value      = uiState.stats.enAttente.toString(),
                                    label      = "EN ATTENTE",
                                    valueColor = GoldPrimary,         // or → en attente
                                    modifier   = Modifier.weight(1f)
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StatCard(
                                    value  = uiState.stats.inscrits.toString(),
                                    label  = "INSCRITS",
                                    modifier = Modifier.weight(1f),
                                    onClick = onInscritsClick
                                )
                                StatCard(
                                    value  = uiState.stats.valides.toString(),
                                    label  = "VALIDÉS",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ── SECTION BASSE : contenu blanc ─────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundWhite)
            ) {

                // ── Paiements en attente ───────────────────────────────────
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(text = "⏳", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "Paiements en attente",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color    = BackgroundWhite,
                    shape    = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    if (uiState.paiementsEnAttente.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text  = "Aucun paiement en attente",
                                color = TextHint
                            )
                        }
                    } else {
                        Column {
                            uiState.paiementsEnAttente.forEachIndexed { index, paiement ->
                                PaiementItem(
                                    paiement    = paiement,
                                    showDivider = index < uiState.paiementsEnAttente.lastIndex,
                                    onClick     = { onPaiementClick(paiement.id) }
                                )
                            }
                        }
                    }
                }

                // ── Prochains événements ───────────────────────────────────
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(text = "📅", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "Prochains événements",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color    = BackgroundWhite,
                    shape    = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    if (uiState.prochainEvenements.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text  = "Aucun événement à venir",
                                color = TextHint
                            )
                        }
                    } else {
                        Column {
                            uiState.prochainEvenements.forEachIndexed { index, evt ->
                                EvenementItem(
                                    evenement   = evt,
                                    showDivider = index < uiState.prochainEvenements.lastIndex
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ── Bottom Navigation Bar ─────────────────────────────────────────────────────

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

private val navItems = listOf(
    NavItem(AdminRoutes.DASHBOARD,   "Dashboard",   Icons.Default.Dashboard),
    NavItem(AdminRoutes.EVENEMENTS,  "Événements",  Icons.Default.CalendarMonth),
    NavItem(AdminRoutes.PAIEMENTS,   "Paiements",   Icons.Default.Payment),
    NavItem(AdminRoutes.INSCRITS,    "Inscrits",    Icons.Default.People),
)

@Composable
private fun AdminBottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = BackgroundWhite,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text     = item.label,
                        fontSize = 11.sp
                    )
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

// ── Prévisualisations ─────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardPreview() {
    GetTelcoAdminTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                stats = DashboardStats(
                    totalEvenements = 8,
                    enAttente       = 12,
                    inscrits        = 120,
                    valides         = 98
                ),
                paiementsEnAttente = listOf(
                    PaiementEnAttente(
                        id             = "1",
                        nomClient      = "Rakoto Jean",
                        montantAr      = 5000,
                        modePaiement   = "MVola",
                        reference      = "TXN-12345678"
                    ),
                    PaiementEnAttente(
                        id             = "2",
                        nomClient      = "Rasoa Marie",
                        montantAr      = 10000,
                        modePaiement   = "Orange Money",
                        reference      = "TXN-87654321"
                    )
                ),
                prochainEvenements = listOf(
                    EvenementProchain(
                        id          = "e1",
                        titre       = "Grande Réception GET 2025",
                        dateDebut   = "17 juil. 2025",
                        lieu        = "Antananarivo",
                        nbInscrits  = 85,
                        capaciteMax = 120
                    )
                ),
                notificationCount = 3
            )
        )
    }
}
