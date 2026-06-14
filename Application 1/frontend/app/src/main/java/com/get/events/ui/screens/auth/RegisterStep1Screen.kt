package com.get.events.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.get.events.navigation.Routes
import com.get.events.ui.components.StepIndicator
import com.get.events.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStep1Screen(
    navController: NavController,
    vm: AuthViewModel = viewModel(
        remember(navController) { navController.getBackStackEntry(Routes.REGISTER_GRAPH) }
    )
) {
    var nom             by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isValid = nom.isNotBlank() && email.contains("@") && password.length >= 6

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
            StepIndicator(currentStep = 1)

            Text(
                text     = "Étape 1 — Informations personnelles",
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

                OutlinedTextField(
                    value         = nom,
                    onValueChange = { nom = it },
                    label         = { Text("Nom complet") },
                    leadingIcon   = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = { Text("Email universitaire") },
                    placeholder   = { Text("nom.prenom@univ.mg") },
                    leadingIcon   = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value         = password,
                    onValueChange = { password = it },
                    label         = { Text("Mot de passe (min. 6 caractères)") },
                    leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon  = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                              else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(12.dp)
                )

                if (password.isNotBlank() && password.length < 6) {
                    Text(
                        text  = "Le mot de passe doit contenir au moins 6 caractères",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick  = {
                        vm.setStep1Data(nom, email, password)
                        navController.navigate(Routes.REGISTER2)
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
