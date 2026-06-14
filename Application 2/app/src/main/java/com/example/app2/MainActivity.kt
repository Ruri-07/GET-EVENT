package com.getticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.getticket.data.api.RepVerification
import com.getticket.ui.theme.GetTicketTheme
import com.getticket.ui.screens.*
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════
// GET Ticket — vérificateur QR (Application 2)
// Base de données partagée avec GET Events
// ═══════════════════════════════════════════════════

sealed class Ecran {
    object Connexion  : Ecran()
    object Scanner    : Ecran()
    object Historique : Ecran()
    data class ResultatValide(val r: RepVerification)  : Ecran()
    data class ResultatRefuse(val r: RepVerification)  : Ecran()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GetTicketTheme {
                AppGetTicket()
            }
        }
    }
}

@Composable
fun AppGetTicket() {
    var ecran by remember { mutableStateOf<Ecran>(Ecran.Connexion) }
    var token by remember { mutableStateOf("") }
    var nom   by remember { mutableStateOf("") }

    when (val e = ecran) {

        // ── Écran 1 : Connexion ──────────────────────────────
        // Vérifie email + mot de passe dans la table utilisateurs (BDD)
        is Ecran.Connexion -> EcranConnexion(
            onConnexionReussie = { t, n ->
                token = t; nom = n
                ecran = Ecran.Scanner
            }
        )

        // ── Écran 2 : Scanner QR ─────────────────────────────
        // Ouvre la caméra, scanne, appelle POST /api/scan
        // Les stats viennent de la BDD via /api/stats
        is Ecran.Scanner -> EcranScanner(
            token = token,
            onResultatRecu = { resultat ->
                ecran = if (resultat.valide)
                    Ecran.ResultatValide(resultat)
                else
                    Ecran.ResultatRefuse(resultat)
            },
            onHistorique = { ecran = Ecran.Historique },
            onDeconnexion = { token = ""; nom = ""; ecran = Ecran.Connexion }
        )

        // ── Écran 3 : Ticket VALIDE ──────────────────────────
        // Affiche les infos du ticket depuis la réponse de la BDD
        is Ecran.ResultatValide -> {
            LaunchedEffect(e) {
                delay(3500)
                ecran = Ecran.Scanner
            }
            EcranResultatValide(
                resultat     = e.r,
                onScannerAutre = { ecran = Ecran.Scanner }
            )
        }

        // ── Écran 4 : Ticket INVALIDE ────────────────────────
        // Affiche la raison du refus depuis la BDD
        is Ecran.ResultatRefuse -> {
            LaunchedEffect(e) {
                delay(3500)
                ecran = Ecran.Scanner
            }
            EcranResultatRefuse(
                resultat           = e.r,
                onScannerAutre     = { ecran = Ecran.Scanner },
                onSignalerProbleme = { /* à implémenter */ }
            )
        }

        // ── Écran 5 : Historique des scans ───────────────────
        // Lit depuis la table historique_scans (BDD)
        is Ecran.Historique -> EcranHistorique(
            token    = token,
            onRetour = { ecran = Ecran.Scanner }
        )
    }
}
