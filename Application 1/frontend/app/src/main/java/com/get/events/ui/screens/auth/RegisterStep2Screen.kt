package com.get.events.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.get.events.navigation.Routes
import com.get.events.ui.components.StepIndicator
import com.get.events.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStep2Screen(
    navController: NavController,
    vm: AuthViewModel = viewModel(
        remember(navController) { navController.getBackStackEntry(Routes.REGISTER_GRAPH) }
    )
) {
    var selectedType  by remember { mutableStateOf("") }
    var annee         by remember { mutableStateOf("") }
    var typeExpanded  by remember { mutableStateOf(false) }
    var anneeExpanded by remember { mutableStateOf(false) }

    val types  = listOf("Étudiant", "Enseignant")
    val annees = listOf("L1", "L2", "L3", "M1", "M2", "Doctorat")
    val mention = "Télécommunications"

    val isEnseignant = selectedType == "Enseignant"
    val isValid = selectedType.isNotBlank() && (isEnseignant || annee.isNotBlank())

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
            StepIndicator(currentStep = 2)

            Text(
                text     = "Étape 2 — Informations universitaires",
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

                // Type de compte
                ExposedDropdownMenuBox(
                    expanded        = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = selectedType,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Type de compte") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier      = Modifier.menuAnchor().fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded        = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text    = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                    if (type == "Enseignant") annee = ""
                                }
                            )
                        }
                    }
                }

                // Mention fixe
                OutlinedTextField(
                    value         = mention,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Mention") },
                    leadingIcon   = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )

                if (!isEnseignant) {
                    ExposedDropdownMenuBox(
                        expanded        = anneeExpanded,
                        onExpandedChange = { anneeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value         = annee,
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Année / Niveau") },
                            placeholder   = { Text("Ex : L3, M1, M2...") },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = anneeExpanded) },
                            modifier      = Modifier.menuAnchor().fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded        = anneeExpanded,
                            onDismissRequest = { anneeExpanded = false }
                        ) {
                            annees.forEach { a ->
                                DropdownMenuItem(
                                    text    = { Text(a) },
                                    onClick = { annee = a; anneeExpanded = false }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick  = {
                        vm.setStep2Data(selectedType, mention, annee)
                        navController.navigate(Routes.REGISTER3)
                    },
                    enabled  = isValid,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(50.dp)
                ) {
                    Text("Suivant →", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
