package com.getticket.database

import at.favre.lib.crypto.bcrypt.BCrypt

/** Compatible avec les mots de passe hashés par GET Events (Application 1). */
fun verifierMotDePasse(saisi: String, stocke: String): Boolean = try {
    BCrypt.verifyer().verify(saisi.toCharArray(), stocke).verified
} catch (_: Exception) {
    false
}
