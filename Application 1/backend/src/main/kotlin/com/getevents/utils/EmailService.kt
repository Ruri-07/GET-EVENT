package com.getevents.utils

import java.util.Properties
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

object EmailService {

    fun sendPasswordResetCode(toEmail: String, code: String, userName: String) {
        val smtpHost = System.getenv("SMTP_HOST")
        val smtpUser = System.getenv("SMTP_USER")
        val smtpPass = System.getenv("SMTP_PASSWORD")
        val smtpFrom = System.getenv("SMTP_FROM") ?: smtpUser ?: "noreply@get.mg"
        val smtpPort = System.getenv("SMTP_PORT")?.toIntOrNull() ?: 587

        val emailSubject = "GET Events — Réinitialisation de mot de passe"
        val body = """
            Bonjour $userName,

            Vous avez demandé la réinitialisation de votre mot de passe GET Events.

            Votre code de vérification : $code

            Ce code expire dans 15 minutes.
            Si vous n'avez pas fait cette demande, ignorez cet email.

            — Équipe GET Events
        """.trimIndent()

        if (smtpHost.isNullOrBlank() || smtpUser.isNullOrBlank() || smtpPass.isNullOrBlank()) {
            println("📧 [DEV] Code de réinitialisation pour $toEmail : $code")
            return
        }

        try {
            val props = Properties().apply {
                put("mail.smtp.host", smtpHost)
                put("mail.smtp.port", smtpPort.toString())
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
            }
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication() =
                    PasswordAuthentication(smtpUser, smtpPass)
            })
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(smtpFrom, "GET Events"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                subject = emailSubject
                setText(body, "UTF-8")
            }
            Transport.send(message)
            println("✅ Email de réinitialisation envoyé à $toEmail")
        } catch (e: Exception) {
            println("⚠️ Erreur envoi email : ${e.message} — Code pour $toEmail : $code")
        }
    }
}
