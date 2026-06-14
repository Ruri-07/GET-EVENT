package com.get.events.ui.components.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.get.events.ui.theme.*

/**
 * Champ texte stylisé GET Telco Admin.
 * Utilisé pour l'email et le mot de passe sur l'écran de connexion.
 */
@Composable
fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false
) {
    val visualTransformation = if (isPassword && !isPasswordVisible)
        PasswordVisualTransformation()
    else
        VisualTransformation.None

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(InputBackground)
        ) {
            OutlinedTextField(
                value          = value,
                onValueChange  = onValueChange,
                label          = { Text(label, color = TextSecondary) },
                singleLine     = true,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                isError        = isError,
                trailingIcon   = {
                    if (isPassword && onTogglePasswordVisibility != null) {
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(
                                imageVector = if (isPasswordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible)
                                    "Masquer le mot de passe"
                                else
                                    "Afficher le mot de passe",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = GreenDark,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor     = MaterialTheme.colorScheme.error,
                    focusedLabelColor    = GreenDark,
                    unfocusedLabelColor  = TextSecondary,
                    cursorColor          = GreenDark,
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary,
                    focusedContainerColor   = InputBackground,
                    unfocusedContainerColor = InputBackground,
                    errorContainerColor     = InputBackground,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
