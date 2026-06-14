package com.get.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.data.model.Event
import com.get.events.ui.theme.*

// ─── Search Bar ──────────────────────────────────────────────────────────────
@Composable
fun GetEventsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFFEEEEEE))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Rechercher",
            tint = TextHint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        BasicTextField_Compat(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Rechercher un événement...",
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Filtres",
            tint = TextSecondary,
            modifier = Modifier
                .size(20.dp)
                .clickable { onFilterClick() }
        )
    }
}

@Composable
fun BasicTextField_Compat(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = TextHint
            )
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─── Featured Event Card (Carousel) ──────────────────────────────────────────
@Composable
fun FeaturedEventCard(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(event.backgroundColor))
            .clickable { onClick() }
    ) {
        // Emoji illustration centré en haut
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (event.categoryEmoji) {
                    "🎉" -> "🎉"
                    "🚌" -> "🚌"
                    else -> event.categoryEmoji
                },
                fontSize = 64.sp
            )
        }

        // Gradient overlay bas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
        )

        // Badge "À LA UNE"
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(YellowBadge)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "⭐ À LA UNE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 12.sp
                )
            )
        }

        // Texte bas
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = "${event.category} · ${event.dateLabel.take(16)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.8f)
                )
            )
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                maxLines = 1
            )
            if (event.hasTickets) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = GreenLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Tickets disponibles",
                        style = MaterialTheme.typography.labelSmall.copy(color = GreenLight)
                    )
                }
            }
        }
    }
}

// ─── Mini Event Card (upcoming) ───────────────────────────────────────────────
@Composable
fun MiniEventCard(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(200.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(event.backgroundColor))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = event.categoryEmoji, fontSize = 40.sp)
    }
}

// ─── Notification Bell with Badge ────────────────────────────────────────────
@Composable
fun NotificationBell(
    unreadCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Notifications",
            tint = TextPrimary,
            modifier = Modifier.size(26.dp)
        )
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(BadgeRed)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
            )
        }
    }
}

// ─── Dot indicators (carousel) ───────────────────────────────────────────────
@Composable
fun CarouselDots(
    count: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == selectedIndex) 24.dp else 8.dp, 8.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (index == selectedIndex) GreenDark else Color(0xFFCCCCCC)
                    )
            )
        }
    }
}
