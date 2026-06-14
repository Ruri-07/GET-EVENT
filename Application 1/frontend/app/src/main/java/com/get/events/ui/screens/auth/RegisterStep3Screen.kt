package com.get.events.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.get.events.navigation.Routes
import com.get.events.ui.components.StepIndicator
import com.get.events.utils.uriToBase64
import com.get.events.viewmodel.AuthUiState
import com.get.events.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStep3Screen(
    navController: NavController,
    vm: AuthViewModel = viewModel(
        remember(navController) { navController.getBackStackEntry(Routes.REGISTER_GRAPH) }
    )
) {
    val context  = LocalContext.current
    val uiState  by vm.uiState.collectAsState()
    val isEnseignant = vm.step2Type == "Enseignant"

    var cardImageUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> cardImageUri = uri }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.REGISTER_GRAPH) { inclusive = true }
                }
            }
            is AuthUiState.RegisterPending -> {
                navController.navigate(Routes.REGISTER_PENDING) {
                    popUpTo(Routes.REGISTER_GRAPH) { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    val stepTitle = if (isEnseignant) "Étape 3 — Photo CIN" else "Étape 3 — Carte étudiant"
    val canSubmit = cardImageUri != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un compte") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            StepIndicator(currentStep = 3)

            Text(
                text     = stepTitle,
                style    = MaterialTheme.typography.labelLarge,
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text  = if (isEnseignant) {
                        "Importez une photo de votre Carte d'Identité Nationale (CIN) pour vérification par l'administrateur."
                    } else {
                        "Importez une photo de votre carte étudiant pour vérification par l'administrateur."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .border(
                            width = 2.dp,
                            color = if (cardImageUri != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { imageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (cardImageUri != null) {
                        AsyncImage(
                            model              = cardImageUri,
                            contentDescription = if (isEnseignant) "Photo CIN" else "Carte étudiant",
                            modifier           = Modifier.fillMaxSize(),
                            contentScale       = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Badge,
                                contentDescription = null,
                                modifier           = Modifier.size(48.dp),
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = if (isEnseignant) "Importer photo CIN" else "Importer carte étudiant",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = "JPG, PNG — max 5 MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (cardImageUri != null) {
                    Text(
                        text  = "✅ Photo sélectionnée — appuyez pour changer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (uiState is AuthUiState.Error) {
                    Text(
                        text  = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text  = if (isEnseignant) {
                        "ℹ️  En confirmant, vous acceptez que votre photo CIN soit vérifiée par l'administration GET."
                    } else {
                        "ℹ️  En confirmant, vous acceptez que votre carte soit vérifiée par l'administration GET."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick  = {
                        val cardBase64 = cardImageUri?.let { context.uriToBase64(it) }
                        vm.register(
                            email             = vm.step1Email,
                            password          = vm.step1Password,
                            fullName          = vm.step1Nom,
                            context           = context,
                            userType          = vm.step2Type,
                            mention           = vm.step2Mention,
                            year              = if (isEnseignant) "" else vm.step2Annee,
                            studentCardBase64 = cardBase64,
                            cin               = null
                        )
                    },
                    enabled  = canSubmit && uiState !is AuthUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(50.dp)
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Créer mon compte", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
