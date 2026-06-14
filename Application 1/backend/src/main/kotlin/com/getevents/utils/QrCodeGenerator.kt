package com.getevents.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.nio.file.Paths

object QrCodeGenerator {

    private val qrCodeDirectory = File("uploads/qrcodes")

    init {
        // Créer le dossier s'il n'existe pas
        if (!qrCodeDirectory.exists()) {
            qrCodeDirectory.mkdirs()
        }
    }

    /**
     * Génère un QR code pour une commande validée
     * @param orderId ID de la commande
     * @param data Données à encoder (ex: "ORDER:123:timestamp")
     * @return Chemin relatif du fichier généré
     */
    fun generateQrCode(orderId: Int, data: String): String {
        // Créer le QR code
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300)

        // Sauvegarder l'image
        val fileName = "order_$orderId.png"
        val filePath = Paths.get(qrCodeDirectory.absolutePath, fileName)
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath)

        // Retourner le chemin relatif pour l'URL
        return "/qrcodes/$fileName"
    }

    /**
     * Génère un QR code avec des données de ticket
     */
    fun generateTicketQrCode(orderId: Int, eventTitle: String, userName: String, ticketCount: Int): String {
        val qrData = """
            {
                "type": "TICKET",
                "orderId": $orderId,
                "event": "$eventTitle",
                "user": "$userName",
                "tickets": $ticketCount,
                "timestamp": "${System.currentTimeMillis()}"
            }
        """.trimIndent()

        return generateQrCode(orderId, qrData)
    }
}