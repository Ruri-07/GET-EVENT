package com.getticket.ui.screens

import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.getticket.data.api.ApiClient
import com.getticket.data.api.RepEvenement
import com.getticket.data.api.RepStats
import com.getticket.data.api.RepVerification
import com.getticket.data.api.toRepVerification
import com.getticket.ui.theme.GreenDark
import com.getticket.ui.theme.GreenMedium
import com.getticket.ui.theme.GreenMint
import com.getticket.ui.theme.YellowBadge
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EcranScanner(
    token: String,
    onResultatRecu: (RepVerification) -> Unit,
    onHistorique: () -> Unit,
    onDeconnexion: () -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current
    val api       = remember { ApiClient() }
    val colors    = MaterialTheme.colorScheme

    var codeDetecte by remember { mutableStateOf<String?>(null) }
    val scanEnCours = remember { AtomicBoolean(false) }
    var stats       by remember { mutableStateOf<RepStats?>(null) }
    var evenement   by remember { mutableStateOf<RepEvenement?>(null) }

    val onResultatState = rememberUpdatedState(onResultatRecu)

    LaunchedEffect(codeDetecte) {
        val code = codeDetecte ?: return@LaunchedEffect
        if (!scanEnCours.compareAndSet(false, true)) return@LaunchedEffect
        try {
            val resultat = api.scannerTicket(code, token).toRepVerification()
            stats = api.chargerStats(token)
            onResultatState.value(resultat)
        } finally {
            scanEnCours.set(false)
            codeDetecte = null
        }
    }

    val onCodeScanned = rememberUpdatedState<(String) -> Unit> { code ->
        if (!scanEnCours.get()) codeDetecte = code
    }

    val permCam = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        stats     = api.chargerStats(token)
        evenement = api.chargerEvenementActif(token)
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Scanner QR Ticket",
                color = GreenDark,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onHistorique,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) { Text("📋", fontSize = 16.sp) }

                IconButton(
                    onClick = onDeconnexion,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) { Text("⏻", fontSize = 16.sp, color = colors.onSurfaceVariant) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primaryContainer)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface),
                    contentAlignment = Alignment.Center
                ) { Text("📅", fontSize = 15.sp) }
                Column {
                    Text(
                        evenement?.nom ?: "Chargement...",
                        color = GreenDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val meta = buildString {
                        append(evenement?.date ?: "")
                        if (evenement?.estActif == true) append(" · Actif")
                    }
                    Text(meta, color = colors.onSurfaceVariant, fontSize = 11.sp)
                }
            }
            if (evenement?.estActif == true) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(GreenMedium)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            if (!permCam.status.isGranted) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text("📷", fontSize = 48.sp)
                    Text(
                        "La permission caméra est nécessaire pour scanner les QR codes",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { permCam.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenDark),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Autoriser la caméra", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val camFuture = ProcessCameraProvider.getInstance(ctx)

                        camFuture.addListener({
                            val camProvider = camFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val analyseur = ImageAnalysis.Builder()
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            analyseur.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                if (scanEnCours.get()) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    BarcodeScanning.getClient()
                                        .process(image)
                                        .addOnSuccessListener { barcodes ->
                                            val code = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                                            if (code != null && !scanEnCours.get()) {
                                                onCodeScanned.value(code)
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            try {
                                camProvider.unbindAll()
                                camProvider.bindToLifecycle(
                                    lifecycle,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    analyseur
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                CadreDeVisee()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Placez le QR code du ticket GET Events dans le cadre",
                color = Color(0xFFB0B0B0),
                fontSize = 13.sp
            )
        }

        Row(modifier = Modifier.fillMaxWidth().background(colors.surface)) {
            StatItem(stats?.valides ?: 0, "Validés", GreenMedium, Modifier.weight(1f), colors)
            HorizontalDivider(
                modifier = Modifier.width(1.dp).height(64.dp).align(Alignment.CenterVertically),
                color = colors.outline
            )
            StatItem(stats?.restants ?: 0, "Restants", YellowBadge, Modifier.weight(1f), colors)
            HorizontalDivider(
                modifier = Modifier.width(1.dp).height(64.dp).align(Alignment.CenterVertically),
                color = colors.outline
            )
            StatItem(stats?.refuses ?: 0, "Refusés", colors.error, Modifier.weight(1f), colors)
        }
    }
}

@Composable
fun CadreDeVisee() {
    Box(modifier = Modifier.size(width = 240.dp, height = 200.dp)) {
        Coin(Alignment.TopStart, haut = true, gauche = true)
        Coin(Alignment.TopEnd, haut = true, gauche = false)
        Coin(Alignment.BottomStart, haut = false, gauche = true)
        Coin(Alignment.BottomEnd, haut = false, gauche = false)
    }
}

@Composable
fun BoxScope.Coin(align: Alignment, haut: Boolean, gauche: Boolean) {
    val ep = 3.dp
    val t = 28.dp
    val r = 4.dp
    Box(Modifier.align(align)) {
        Box(
            Modifier.width(t).height(ep)
                .align(if (haut) Alignment.TopStart else Alignment.BottomStart)
                .background(GreenMint, RoundedCornerShape(
                    topStart = if (gauche && haut) r else 0.dp,
                    topEnd = if (!gauche && haut) r else 0.dp,
                    bottomStart = if (gauche && !haut) r else 0.dp,
                    bottomEnd = if (!gauche && !haut) r else 0.dp
                ))
        )
        Box(
            Modifier.width(ep).height(t)
                .align(if (gauche) Alignment.TopStart else Alignment.TopEnd)
                .background(GreenMint, RoundedCornerShape(
                    topStart = if (gauche && haut) r else 0.dp,
                    topEnd = if (!gauche && haut) r else 0.dp,
                    bottomStart = if (gauche && !haut) r else 0.dp,
                    bottomEnd = if (!gauche && !haut) r else 0.dp
                ))
        )
    }
}

@Composable
fun StatItem(
    valeur: Int,
    label: String,
    couleur: Color,
    modifier: Modifier = Modifier,
    colors: ColorScheme = MaterialTheme.colorScheme
) {
    Column(
        modifier = modifier.padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valeur.toString(), color = couleur, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = colors.onSurfaceVariant, fontSize = 11.sp)
    }
}

private suspend fun ApiClient.chargerEvenementActif(token: String) = chargerEvenement(token)
