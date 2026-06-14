package com.get.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.get.events.data.local.TokenDataStore
import com.get.events.viewmodel.EventsUiState
import com.get.events.viewmodel.EventsViewModel
import com.get.events.viewmodel.NotificationsViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.data.EventsRefreshNotifier
import com.get.events.data.model.Event
import com.get.events.ui.components.*
import com.get.events.ui.theme.*

@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    vm: EventsViewModel = viewModel(),
    notifVm: NotificationsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by vm.uiState.collectAsState()
    var userName by remember { mutableStateOf("Visiteur") }
    var isVisitor by remember { mutableStateOf(true) }
    val unreadCount by notifVm.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        TokenDataStore.getUserName(context).collect { name ->
            if (!name.isNullOrBlank()) userName = name
        }
    }
    LaunchedEffect(Unit) {
        TokenDataStore.isVisitor(context).collect { isVisitor = it }
    }
    LaunchedEffect(isVisitor) {
        if (!isVisitor) notifVm.loadUnreadCount()
    }
    val refreshKey by EventsRefreshNotifier.trigger.collectAsState()
    LaunchedEffect(refreshKey) {
        vm.loadEvents()
    }

    val (featuredEvents, upcomingEvents) = when (val state = uiState) {
        is EventsUiState.Ready -> state.featured to state.upcoming
        else -> emptyList<Event>() to emptyList()
    }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Event>?>(null) }
    var carouselIndex by remember { mutableStateOf(0) }

    LaunchedEffect(featuredEvents.size) {
        if (carouselIndex >= featuredEvents.size) {
            carouselIndex = 0
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            vm.searchEvents(searchQuery) { searchResults = it }
        } else {
            searchResults = null
        }
    }

    val scrollState = rememberScrollState()
    val isSearching = searchQuery.length >= 2
    val displayedUpcoming = if (isSearching) searchResults ?: emptyList() else upcomingEvents

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bonjour 👋",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            // Cloche
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onNavigateToNotifications() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
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
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GreenMint)
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil",
                    tint = GreenDark,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Search bar ────────────────────────────────────────────────────
        GetEventsSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onFilterClick = { /* ouvrir filtres */ },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(20.dp))

        if (isSearching) {
            Text(
                text = if (displayedUpcoming.isEmpty()) "Aucun résultat pour « $searchQuery »"
                else "Résultats pour « $searchQuery »",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
            displayedUpcoming.forEach { event ->
                FeaturedEventCard(
                    event = event,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    onClick = { onEventClick(event.id) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // ── Carousel "À LA UNE" ───────────────────────────────────────────
        if (!isSearching && featuredEvents.isNotEmpty()) {
            FeaturedEventCard(
                event = featuredEvents[carouselIndex],
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick = { onEventClick(featuredEvents[carouselIndex].id) }
            )

            Spacer(Modifier.height(12.dp))

            // Dots
            CarouselDots(
                count = featuredEvents.size,
                selectedIndex = carouselIndex,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Boutons navigation carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (carouselIndex > 0) {
                    TextButton(onClick = { carouselIndex-- }) {
                        Text("‹", fontSize = 22.sp, color = GreenDark)
                    }
                } else Spacer(Modifier.width(40.dp))

                if (carouselIndex < featuredEvents.size - 1) {
                    TextButton(onClick = { carouselIndex++ }) {
                        Text("›", fontSize = 22.sp, color = GreenDark)
                    }
                } else Spacer(Modifier.width(40.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        if (!isSearching) {
            Text(
                text = "Prochains événements",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(upcomingEvents) { event ->
                    MiniEventCard(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
