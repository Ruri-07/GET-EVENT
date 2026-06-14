package com.getevents.utils

import java.io.File
import java.util.Base64

object StudentCardStorage {

    private val uploadDir = File("./uploads/student-cards")

    fun saveFromBase64(base64: String, userId: Int): String? {
        return try {
            uploadDir.mkdirs()
            val clean = base64.substringAfter("base64,").trim()
            val bytes = Base64.getDecoder().decode(clean)
            val file = File(uploadDir, "user_$userId.jpg")
            file.writeBytes(bytes)
            "/student-cards/user_$userId.jpg"
        } catch (_: Exception) {
            null
        }
    }
}
