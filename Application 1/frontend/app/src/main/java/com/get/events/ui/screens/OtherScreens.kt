package com.get.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.data.model.TicketStatus
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.get.events.data.EventsRefreshNotifier
import com.get.events.data.local.TokenDataStore
import com.get.events.data.repository.FakeData
import com.get.events.viewmodel.EventsUiState
import com.get.events.viewmodel.EventsViewModel
import com.get.events.viewmodel.OrdersViewModel
import com.get.events.ui.theme.*
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.get.events.data.api.RetrofitClient
// ─── EventsScreen ─────────────────────────────────────────────────────────────
@Composable
fun EventsScreen(
    onEventClick: (String) -> Unit = {},
    vm: EventsViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val refreshKey by EventsRefreshNotifier.trigger.collectAsState()
    LaunchedEffect(refreshKey) {
        vm.loadEvents()
    }
    val allEvents = when (val state = uiState) {
        is EventsUiState.Ready -> state.upcoming.distinctBy { it.id }
        else -> emptyList()
    }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<com.get.events.data.model.Event>?>(null) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            vm.searchEvents(searchQuery) { searchResults = it }
        } else {
            searchResults = null
        }
    }

    val displayedEvents = searchResults ?: allEvents

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("Événements", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(12.dp))
            com.get.events.ui.components.GetEventsSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onFilterClick = {}
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(displayedEvents) { event ->
                EventListCard(event = event, onClick = { onEventClick(event.id) })
            }
        }
    }
}

@Composable
private fun EventListCard(
    event: com.get.events.data.model.Event,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(event.backgroundColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = event.categoryEmoji, fontSize = 28.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(
                text = event.dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                text = event.location,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

// ─── MyTicketsScreen ──────────────────────────────────────────────────────────
@Composable
fun MyTicketsScreen(
    onTicketClick: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    vm: OrdersViewModel = viewModel()
) {
    val context = LocalContext.current
    var isVisitor by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        TokenDataStore.isVisitor(context).collect { isVisitor = it }
    }
    LaunchedEffect(isVisitor) {
        if (!isVisitor) {
            while (true) {
                vm.loadOrders()
                delay(4000)
            }
        }
    }

    val orders by vm.orders.collectAsState()
    val tickets = remember(orders) { vm.ordersAsTickets() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("Mes Tickets", style = MaterialTheme.typography.displayLarge)
        }

        Spacer(Modifier.height(12.dp))

        if (isVisitor) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text("🎟️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Connectez-vous pour voir vos tickets",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                    ) {
                        Text("S'inscrire / Se connecter")
                    }
                }
            }
        } else if (tickets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎟️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Aucun ticket pour l'instant",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tickets) { ticket ->
                    TicketCard(
                        ticket = ticket,
                        onClick = { onTicketClick(ticket.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: com.get.events.data.model.Ticket,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GreenSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = GreenDark
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(ticket.eventTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(ticket.eventDate, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text(ticket.eventLocation, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(
                    when (ticket.status) {
                        TicketStatus.VALID -> GreenSurface
                        TicketStatus.PENDING -> Color(0xFFFFF3E0)
                        TicketStatus.USED -> Color(0xFFEEEEEE)
                        TicketStatus.EXPIRED -> Color(0xFFFFEBEE)
                    }
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = when (ticket.status) {
                    TicketStatus.VALID -> "Valide"
                    TicketStatus.PENDING -> "En attente"
                    TicketStatus.USED -> "Utilisé"
                    TicketStatus.EXPIRED -> "Expiré"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = when (ticket.status) {
                        TicketStatus.VALID -> GreenDark
                        TicketStatus.PENDING -> Color(0xFFE65100)
                        TicketStatus.USED -> TextSecondary
                        TicketStatus.EXPIRED -> Color(0xFFD32F2F)
                    },
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

// ─── TicketDetailScreen ───────────────────────────────────────────────────────
@Composable
fun TicketDetailScreen(
    ticketId: String,
    onBack: () -> Unit = {},
    vm: OrdersViewModel = viewModel()
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.loadOrders()
        TokenDataStore.getUserName(context).collect { name ->
            if (!name.isNullOrBlank()) userName = name
        }
    }

    val orders by vm.orders.collectAsState()
    val ticket = remember(orders, ticketId) {
        vm.ordersAsTickets().find { it.id == ticketId }
    }

    if (ticket == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenDark)
        }
        return
    }

    val isPending = ticket.status == TicketStatus.PENDING
    val isValid = ticket.status == TicketStatus.VALID
    val organizerLabel = "GET · Télécommunications"
    val holderType = "Étudiant"
    val ticketCode = "#GET-${ticket.id.padStart(4, '0')}"
    val qrUrl = RetrofitClient.mediaUrl(ticket.qrCode)

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = TextPrimary,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(24.dp)
                        .clickable { onBack() }
                )
                Text(
                    text = "Mon Ticket",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Center)
                )
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Partager",
                    tint = TextPrimary,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp)
                        .clickable { }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceWhite)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenDark)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(
                            text = organizerLabel,
                            fontSize = 12.sp,
                            color = GreenLight,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = ticket.eventTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text(text = "Titulaire", fontSize = 11.sp, color = GreenLight)
                                Text(
                                    text = userName.ifBlank { "Étudiant GET" },
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column {
                                Text(text = "Type", fontSize = 11.sp, color = GreenLight)
                                Text(
                                    text = holderType,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when (ticket.status) {
                                    TicketStatus.VALID -> Color(0xFF52B788)
                                    TicketStatus.PENDING -> Color(0xFFFF9800)
                                    TicketStatus.USED -> Color(0xFF9CA3AF)
                                    TicketStatus.EXPIRED -> Color(0xFFEF4444)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isValid) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = when (ticket.status) {
                                    TicketStatus.VALID -> "VALIDÉ"
                                    TicketStatus.PENDING -> "EN ATTENTE"
                                    TicketStatus.USED -> "UTILISÉ"
                                    TicketStatus.EXPIRED -> "EXPIRÉ"
                                },
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(GreenDark)
                ) {
                    val notchRadius = 10.dp.toPx()
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(0f, 0f)
                    path.lineTo(0f, size.height)
                    path.arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            left = -notchRadius,
                            top = size.height - notchRadius * 2,
                            right = notchRadius,
                            bottom = size.height
                        ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                    path.lineTo(size.width - notchRadius, size.height)
                    path.arcTo(
                        rect = androidx.compose.ui.geometry.Rect(
                            left = size.width - notchRadius,
                            top = size.height - notchRadius * 2,
                            right = size.width + notchRadius,
                            bottom = size.height
                        ),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                    path.lineTo(size.width, 0f)
                    path.close()
                    drawPath(path, color = androidx.compose.ui.graphics.Color(0xFF1B4332))
                    drawLine(
                        color = androidx.compose.ui.graphics.Color(0xFFE5E7EB),
                        start = androidx.compose.ui.geometry.Offset(notchRadius * 2, size.height / 2),
                        end = androidx.compose.ui.geometry.Offset(size.width - notchRadius * 2, size.height / 2),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(8f, 6f), 0f
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isPending -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("⏳", fontSize = 48.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "QR Code disponible après validation par l'administrateur",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        color = TextSecondary
                                    )
                                }
                            }
                            qrUrl != null -> {
                                AsyncImage(
                                    model = qrUrl,
                                    contentDescription = "QR Code du ticket",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            else -> {
                                QrCodePlaceholder(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .padding(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = userName.ifBlank { "Étudiant GET" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$ticketCode · Étudiant",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (isPending) {
                            "Votre paiement est en cours de vérification"
                        } else {
                            "Présentez ce QR code à l'entrée"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextHint,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            if (isValid) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFD8F3DC))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(GreenMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Ticket valide · ${ticket.eventDate}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GreenDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

// ─── QR code placeholder (remplacé par AsyncImage en prod) ───────────────────
@Composable
private fun QrCodePlaceholder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cellSize = size.width / 7f
        val color = GreenDark

        fun drawCell(col: Int, row: Int) {
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(col * cellSize, row * cellSize),
                size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
            )
        }

        // Coin supérieur gauche — carré repère
        for (r in 0..6) for (c in 0..6) {
            val inner = r in 2..4 && c in 2..4
            val outer = r == 0 || r == 6 || c == 0 || c == 6
            if (outer || inner) drawCell(c, r)
        }
        // Coin supérieur droit
        for (r in 0..6) for (c in 0..6) {
            val col = c + (7 - 0) + 1 // décalage
            if (col < 7) continue
            val adjC = col - 8
            if (adjC < 0 || adjC > 6) continue
            val inner = r in 2..4 && adjC in 2..4
            val outer = r == 0 || r == 6 || adjC == 0 || adjC == 6
            if (outer || inner) drawCell(col, r)
        }
        // Coin inférieur gauche
        for (r in 0..6) for (c in 0..6) {
            val row = r + 8
            val inner = r in 2..4 && c in 2..4
            val outer = r == 0 || r == 6 || c == 0 || c == 6
            if (outer || inner) drawCell(c, row)
        }
        // Quelques modules de données (simulés)
        val dataModules = listOf(
            Pair(8, 2), Pair(9, 2), Pair(10, 3), Pair(8, 4),
            Pair(9, 5), Pair(11, 2), Pair(12, 4), Pair(8, 6),
            Pair(2, 8), Pair(4, 8), Pair(5, 9), Pair(3, 10),
            Pair(6, 8), Pair(2, 11), Pair(5, 12), Pair(4, 11),
            Pair(8, 8), Pair(9, 9), Pair(10, 8), Pair(11, 10),
            Pair(12, 9), Pair(8, 11), Pair(10, 12), Pair(12, 11)
        )
        for ((c, r) in dataModules) {
            if (c < 14 && r < 14) drawCell(c, r)
        }
    }
}
