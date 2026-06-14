package com.getticket.models

import kotlinx.serialization.Serializable

@Serializable
data class ReqConnexion(val email: String, val motDePasse: String)

@Serializable
data class ReqVerification(val codeQR: String)

@Serializable
data class ReqScan(val code: String)

@Serializable
data class RepScanResult(
    val valid: Boolean,
    val message: String,
    val nomClient: String   = "",
    val categorie: String     = "",
    val promotion: String     = "",
    val codeTicket: String    = "",
    val typeTicket: String    = "",
    val raisonRefus: String   = ""
)

@Serializable
data class RepConnexion(
    val success: Boolean = false, val token: String = "",
    val nom: String = "", val message: String = ""
)

@Serializable
data class RepVerification(
    val valide: Boolean,
    val message: String,
    val nomClient: String            = "",
    val categorie: String            = "",
    val promotion: String            = "",
    val codeTicket: String           = "",
    val typeTicket: String           = "",
    val premiereUtilisation: Boolean = true,
    val dateUtilisation: String      = "",
    val raisonRefus: String          = ""
)

@Serializable
data class RepEvenement(val nom: String, val date: String, val estActif: Boolean)

@Serializable
data class RepStats(val valides: Int, val restants: Int, val refuses: Int)

@Serializable
data class LigneScan(
    val nomClient: String, val categorie: String,
    val heureUtilisation: String, val statut: String,
    val raisonRefus: String = ""
)

@Serializable
data class RepStatsHistorique(val valides: Int, val refuses: Int, val doublons: Int)

@Serializable
data class RepHistorique(val lignes: List<LigneScan>, val stats: RepStatsHistorique)
