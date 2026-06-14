package com.get.events.ui.screens.admin.paiement

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.get.events.data.model.admin.*
import com.get.events.ui.theme.*

/**
 * Écran de validation / rejet d'un paiement.
 *
 * Architecture stateless :
 *  - [uiState] injecté depuis [ValidatePaiementViewModel]
 *  - Les données affichées (nom, filière, ticket, contact, référence) sont
 *    chargées depuis la base de données via GET /api/admin/payments/{id}
 *  - Les boutons déclenchent [onValider] / [onRejeter] → appels BDD backend
 *
 * Flux BDD :
 *  Valider → POST /api/admin/payments/{id}/validate
 *           → BDD : statut = VALIDE  +  génération ticket QR  +  envoi auto
 *  Rejeter → POST /api/admin/payments/{id}/reject
 *           → BDD : statut = REJETE  +  notification inscrit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidatePaiementScreen(
    uiState: ValidatePaiementUiState,
    onBack: () -> Unit                = {},
    onValider: () -> Unit             = {},
    onRejeter: () -> Unit             = {},
    onVoirCarte: () -> Unit           = {},
    onActionDismissed: () -> Unit     = {},
    modifier: Modifier                = Modifier
) {
    // Dialog de confirmation avant action irréversible
    var showConfirmValider by remember { mutableStateOf(false) }
    var showConfirmRejeter by remember { mutableStateOf(false) }

    // Dialog résultat après action (succès ou erreur)
    val actionResult = uiState.actionResult

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text       = "Valider\nPaiement",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp,
                        lineHeight = 26.sp,
                        color      = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint               = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        containerColor = Color(0xFFF0F2F0),
        modifier       = modifier
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // ── Loader principal (chargement BDD) ─────────────────────────
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color    = GreenDark,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // ── Erreur chargement BDD ─────────────────────────────────────
            uiState.errorMessage?.let { msg ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.error,
                        modifier           = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text  = msg,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // ── Contenu principal ─────────────────────────────────────────
            uiState.paiement?.let { paiement ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ── Carte principale : infos inscrit + paiement (BDD) ──
                    InfoCard(paiement = paiement)

                    // ── Carte étudiant importée ────────────────────────────
                    if (paiement.hasCarteEtudiante) {
                        CarteEtudianteRow(onVoirCarte = onVoirCarte)
                    }

                    // ── Message informatif ─────────────────────────────────
                    Text(
                        text  = "Après validation, le ticket QR sera automatiquement envoyé à l'étudiant.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color    = TextSecondary,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Bouton Valider ─────────────────────────────────────
                    Button(
                        onClick  = { showConfirmValider = true },
                        enabled  = !uiState.isActionLoading,
                        shape    = RoundedCornerShape(50.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = GreenDark,
                            contentColor           = TextOnGreen,
                            disabledContainerColor = GreenDark.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        if (uiState.isActionLoading) {
                            CircularProgressIndicator(
                                color       = TextOnGreen,
                                strokeWidth = 2.dp,
                                modifier    = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector        = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier           = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text       = "Valider et envoyer le ticket",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp
                            )
                        }
                    }

                    // ── Bouton Rejeter (outline rouge) ─────────────────────
                    OutlinedButton(
                        onClick  = { showConfirmRejeter = true },
                        enabled  = !uiState.isActionLoading,
                        shape    = RoundedCornerShape(50.dp),
                        border   = androidx.compose.foundation.BorderStroke(
                            2.dp, Color(0xFFD32F2F)
                        ),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor         = Color(0xFFD32F2F),
                            disabledContentColor = Color(0xFFD32F2F).copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text       = "Rejeter le paiement",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // ── Dialog confirmation Valider ───────────────────────────────────────
    if (showConfirmValider) {
        ConfirmActionDialog(
            title       = "Confirmer la validation",
            message     = "Le ticket QR sera généré et envoyé automatiquement à l'inscrit. Cette action est irréversible.",
            confirmText = "Valider",
            confirmColor = GreenDark,
            onConfirm   = {
                showConfirmValider = false
                onValider()
            },
            onDismiss   = { showConfirmValider = false }
        )
    }

    // ── Dialog confirmation Rejeter ───────────────────────────────────────
    if (showConfirmRejeter) {
        ConfirmActionDialog(
            title       = "Confirmer le rejet",
            message     = "L'inscrit sera notifié du rejet de son paiement. Cette action est irréversible.",
            confirmText = "Rejeter",
            confirmColor = Color(0xFFD32F2F),
            onConfirm   = {
                showConfirmRejeter = false
                onRejeter()
            },
            onDismiss   = { showConfirmRejeter = false }
        )
    }

    // ── Dialog résultat action (BDD response) ─────────────────────────────
    actionResult?.let { result ->
        ActionResultDialog(
            result    = result,
            onDismiss = onActionDismissed
        )
    }
}

// ── Composants internes ───────────────────────────────────────────────────────

/**
 * Carte blanche avec accent vert à gauche.
 * Affiche toutes les infos de l'inscrit et du paiement venant de la BDD.
 */
@Composable
private fun InfoCard(paiement: PaiementDetail) {
    Surface(
        color           = Color.White,
        shape           = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Row {
            // Bande verte gauche (accent de la maquette)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(GreenDark)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Nom complet (BDD : table Inscrit)
                Text(
                    text       = paiement.nomComplet,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 20.sp,
                    color      = TextPrimary
                )

                Divider(
                    color     = DividerColor,
                    thickness = 1.dp,
                    modifier  = Modifier.padding(vertical = 10.dp)
                )

                // Filière (BDD : table Inscrit)
                InfoRow(
                    icon  = Icons.Default.School,
                    label = "${paiement.typeInscrit} · ${paiement.filiere}"
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Ticket (BDD : table Ticket)
                InfoRow(
                    icon  = Icons.Default.ConfirmationNumber,
                    label = "${paiement.quantite} × ${paiement.typeTicket} — ${formatMontant(paiement.montantAr)} Ar"
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Téléphone + mode paiement (BDD : table Paiement)
                InfoRow(
                    icon  = Icons.Default.Phone,
                    label = "${paiement.telephone} (${paiement.modePaiement})"
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Référence transaction (BDD : table Paiement)
                InfoRow(
                    icon  = Icons.Default.Receipt,
                    label = "Réf: ${paiement.reference}"
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = GreenDark,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color    = TextPrimary,
                fontSize = 14.sp
            )
        )
    }
}

/**
 * Ligne "Carte étudiant importée — Voir"
 * La carte est stockée en BDD (URL dans table Document).
 */
@Composable
private fun CarteEtudianteRow(onVoirCarte: () -> Unit) {
    Surface(
        color  = Color(0xFFECF3EF),
        shape  = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.Badge,
                    contentDescription = null,
                    tint               = TextSecondary,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text  = "Carte étudiant importée — ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )
            }
            TextButton(
                onClick      = onVoirCarte,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text       = "Voir",
                    color      = GreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )
            }
        }
    }
}

/**
 * Dialog de confirmation avant action irréversible sur la BDD.
 */
@Composable
private fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = message, color = TextSecondary)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondary)
            }
        },
        containerColor = Color.White,
        shape          = RoundedCornerShape(16.dp)
    )
}

/**
 * Dialog résultat — affiché après réponse de la BDD (succès ou erreur).
 */
@Composable
private fun ActionResultDialog(
    result: ActionResult,
    onDismiss: () -> Unit
) {
    val (icon, title, message, iconTint) = when (result) {
        is ActionResult.ValidationSuccess -> listOf(
            Icons.Default.CheckCircle,
            "Paiement validé !",
            result.message,
            GreenDark
        )
        is ActionResult.RejetSuccess -> listOf(
            Icons.Default.Cancel,
            "Paiement rejeté",
            result.message,
            Color(0xFFD32F2F)
        )
        is ActionResult.Error -> listOf(
            Icons.Default.ErrorOutline,
            "Erreur",
            result.message,
            Color(0xFFD32F2F)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector        = icon as ImageVector,
                contentDescription = null,
                tint               = iconTint as Color,
                modifier           = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text       = title as String,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
        },
        text = {
            Text(text = message as String, color = TextSecondary)
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors  = ButtonDefaults.buttonColors(containerColor = GreenDark)
            ) {
                Text("OK", color = Color.White)
            }
        },
        containerColor = Color.White,
        shape          = RoundedCornerShape(16.dp)
    )
}

// ── Utilitaires ───────────────────────────────────────────────────────────────

private fun formatMontant(montant: Int): String =
    montant.toString().reversed().chunked(3).joinToString(" ").reversed()

// ── Prévisualisation ──────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ValidatePaiementPreview() {
    GetTelcoAdminTheme {
        ValidatePaiementScreen(
            uiState = ValidatePaiementUiState(
                paiement = PaiementDetail(
                    id               = "1",
                    nomComplet       = "Rakoto Jean",
                    typeInscrit      = "Étudiant",
                    filiere          = "L3 Télécommunications",
                    telephone        = "034 87 654 32",
                    typeTicket       = "Ticket Étudiant",
                    quantite         = 1,
                    montantAr        = 5000,
                    modePaiement     = "MVola",
                    reference        = "TXN-12345678",
                    statut           = StatutPaiement.EN_ATTENTE,
                    carteEtudiantUrl = "https://cdn.get-telco.mg/cartes/rakoto_jean.jpg"
                )
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ValidatePaiementLoadingPreview() {
    GetTelcoAdminTheme {
        ValidatePaiementScreen(
            uiState = ValidatePaiementUiState(isLoading = true)
        )
    }
}
