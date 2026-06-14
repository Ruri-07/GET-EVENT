package com.getticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.getticket.data.api.ApiClient
import com.getticket.ui.theme.GreenDark
import com.getticket.ui.theme.GreenMedium
import kotlinx.coroutines.launch

@Composable
fun EcranConnexion(
    onConnexionReussie: (token: String, nom: String) -> Unit
) {
    var email   by remember { mutableStateOf("") }
    var mdp     by remember { mutableStateOf("") }
    var erreur  by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope   = rememberCoroutineScope()
    val api     = remember { ApiClient() }
    val colors  = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(colors.primaryContainer, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🔍", fontSize = 38.sp) }

            Spacer(Modifier.height(20.dp))
            Text("GET Ticket", color = GreenDark, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Text("Vérificateur", color = GreenMedium, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Scanner QR pour valider les tickets GET Events",
                color = colors.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column {
                        Text("Identifiant Admin", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; erreur = "" },
                            placeholder = { Text("admin@get.mg", color = colors.onSurfaceVariant) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = GreenMedium,
                                unfocusedBorderColor = colors.outline,
                                focusedTextColor     = colors.onSurface,
                                unfocusedTextColor   = colors.onSurface,
                                cursorColor          = GreenMedium
                            )
                        )
                    }

                    Column {
                        Text("Mot de passe", color = GreenMedium, fontSize = 12.sp)
                        OutlinedTextField(
                            value = mdp,
                            onValueChange = { mdp = it; erreur = "" },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = GreenMedium,
                                unfocusedBorderColor = colors.outline,
                                focusedTextColor     = colors.onSurface,
                                unfocusedTextColor   = colors.onSurface,
                                cursorColor          = GreenMedium
                            )
                        )
                    }
                }
            }

            if (erreur.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(erreur, color = colors.error, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isBlank() || mdp.isBlank()) {
                        erreur = "Veuillez remplir tous les champs"; return@Button
                    }
                    scope.launch {
                        loading = true
                        val rep = api.seConnecter(email, mdp)
                        if (rep.success) onConnexionReussie(rep.token, rep.nom)
                        else erreur = rep.message
                        loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark),
                enabled = !loading
            ) {
                if (loading)
                    CircularProgressIndicator(Modifier.size(22.dp), color = colors.onPrimary, strokeWidth = 2.dp)
                else
                    Text("Accéder au scanner", color = colors.onPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Compte admin GET Events · Base partagée",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}
