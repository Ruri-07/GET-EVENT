package com.get.events.ui.screens.auth

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.get.events.R
import com.get.events.navigation.Routes
import com.get.events.navigation.admin.AdminRoutes
import com.get.events.viewmodel.AuthUiState
import com.get.events.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    vm: AuthViewModel = viewModel()
) {
    val context  = LocalContext.current
    val uiState  by vm.uiState.collectAsState()

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                if (state.user.role == "ADMIN") {
                    navController.navigate(AdminRoutes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                } else {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connexion") },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(8.dp))

            Image(
                painter            = painterResource(R.drawable.logo),
                contentDescription = "Logo GET",
                modifier           = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            OutlinedTextField(
                value       = email,
                onValueChange = { email = it },
                label       = { Text("Email universitaire") },
                placeholder = { Text("nom.prenom@univ.mg") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier    = Modifier.fillMaxWidth(),
                singleLine  = true,
                shape       = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value       = password,
                onValueChange = { password = it },
                label       = { Text("Mot de passe") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        vm.resetState()
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    }
                ) {
                    Text("Mot de passe oublié ?")
                }
            }

            if (uiState is AuthUiState.Error) {
                Text(
                    text  = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick  = { vm.login(email, password, context) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = email.isNotBlank() && password.isNotBlank()
                           && uiState !is AuthUiState.Loading,
                shape    = RoundedCornerShape(50.dp)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Se connecter", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
