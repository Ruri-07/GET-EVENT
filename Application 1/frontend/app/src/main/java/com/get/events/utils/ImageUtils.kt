package com.get.events.utils

import android.content.Context
import android.net.Uri
import android.util.Base64

fun Context.uriToBase64(uri: Uri): String? {
    return try {
        contentResolver.openInputStream(uri)?.use { input ->
            Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
        }
    } catch (_: Exception) {
        null
    }
}
