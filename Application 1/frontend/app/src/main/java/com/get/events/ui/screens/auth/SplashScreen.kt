package com.get.events.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.get.events.R
import com.get.events.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var visibleLetters by remember { mutableStateOf(0) }
    var showWords      by remember { mutableStateOf(false) }
    var showSubtitle   by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300);  visibleLetters = 1
        delay(300);  visibleLetters = 2
        delay(300);  visibleLetters = 3
        delay(600);  showWords    = true
        delay(800);  showSubtitle = true
        delay(600)
        navController.navigate(Routes.WELCOME) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    val subtitleAlpha by animateFloatAsState(
        targetValue    = if (showSubtitle) 1f else 0f,
        animationSpec  = tween(1000),
        label          = "subtitleAlpha"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier          = Modifier.fillMaxSize(),
            contentAlignment  = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Image(
                    painter            = painterResource(R.drawable.logo),
                    contentDescription = "GET Logo",
                    modifier           = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.Top
                ) {
                    listOf(
                        Triple("G", "Groupe des",   0),
                        Triple("E", "Étudiants en", 1),
                        Triple("T", "Télécoms",     2)
                    ).forEach { (letter, word, index) ->
                        val letterVisible = visibleLetters > index

                        val letterScale by animateFloatAsState(
                            targetValue   = if (letterVisible) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness    = Spring.StiffnessLow
                            ),
                            label = "scale_$letter"
                        )
                        val wordAlpha by animateFloatAsState(
                            targetValue   = if (showWords && letterVisible) 1f else 0f,
                            animationSpec = tween(500, delayMillis = index * 150),
                            label         = "wordAlpha_$letter"
                        )
                        val wordOffset by animateFloatAsState(
                            targetValue   = if (showWords && letterVisible) 0f else 20f,
                            animationSpec = tween(500, delayMillis = index * 150),
                            label         = "wordOffset_$letter"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.width(90.dp)
                        ) {
                            Text(
                                text      = letter,
                                style     = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                modifier  = Modifier.scale(letterScale),
                                color     = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text     = word,
                                style    = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color    = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .alpha(wordAlpha)
                                    .offset(y = wordOffset.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text     = "EVENTS",
                    style    = MaterialTheme.typography.displayMedium,
                    color    = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.alpha(subtitleAlpha)
                )
            }
        }
    }
}
