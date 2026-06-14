package com.get.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.get.events.data.model.MobileOperator
import androidx.lifecycle.viewmodel.compose.viewModel
import com.get.events.data.repository.TicketTypeHelper
import com.get.events.viewmodel.EventsViewModel
import com.get.events.viewmodel.OrdersViewModel
import com.get.events.ui.components.PurchaseStepper
import com.get.events.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MobilePaymentScreen(
    ticketTypeId: String,
    eventId: String,
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {},
    ordersVm: OrdersViewModel = viewModel(),
    eventsVm: EventsViewModel = viewModel()
) {
    LaunchedEffect(eventId) { eventsVm.loadEvent(eventId) }
    val event by eventsVm.selectedEvent.collectAsState()

    val ticketTypes = remember(event) {
        event?.let { TicketTypeHelper.ticketTypesForEvent(it) } ?: emptyList()
    }
    val ticketType = ticketTypes.find { it.id == ticketTypeId }
        ?: ticketTypes.firstOrNull()

    var selectedOperator by remember { mutableStateOf(MobileOperator.MVOLA) }
    var studentName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var transactionRef by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val phoneBringIntoView = remember { BringIntoViewRequester() }
    val refBringIntoView = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val operatorPhone = when (selectedOperator) {
        MobileOperator.ORANGE_MONEY -> "032 XX XXX XX"
        MobileOperator.MVOLA -> "034 XX XXX XX"
        MobileOperator.AIRTEL_MONEY -> "033 XX XXX XX"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
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
                    text = "Paiement\nMobile Money",
                    style = MaterialTheme.typography.displayMedium.copy(
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(24.dp))

            PurchaseStepper(
                currentStep = 2,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                event?.let { evt ->
                    Text(
                        text = evt.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (ticketType == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenDark)
                    }
                } else Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GreenMint)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Récapitulatif",
                            style = MaterialTheme.typography.labelSmall.copy(color = GreenDark)
                        )
                        Text(
                            text = "${ticketType.name} × 1",
                            style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
                        )
                    }
                    Text(
                        text = ticketType.priceLabel,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GreenDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (ticketType != null) Spacer(Modifier.height(24.dp))

                if (ticketType != null) Text(
                    text = "Choisir le mode de paiement",
                    style = MaterialTheme.typography.titleLarge
                )

                if (ticketType != null) Spacer(Modifier.height(14.dp))

                if (ticketType != null) Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MobileOperator.values().forEach { operator ->
                        val isSelected = selectedOperator == operator
                        OperatorCard(
                            operator = operator,
                            isSelected = isSelected,
                            onClick = { selectedOperator = operator },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (ticketType != null) Spacer(Modifier.height(20.dp))

                if (ticketType != null) Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF9F0))
                        .padding(14.dp)
                ) {
                    Text(text = "📌", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Envoyez ",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )
                    Text(
                        text = ticketType!!.priceLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = " au $operatorPhone (GET), puis remplissez ce formulaire :",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )
                }

                if (ticketType != null) Spacer(Modifier.height(20.dp))

                if (ticketType != null) PaymentTextField(
                    label = "Nom de l'étudiant",
                    value = studentName,
                    onValueChange = { studentName = it },
                    placeholder = "Rakoto Jean"
                )

                if (ticketType != null) Spacer(Modifier.height(14.dp))

                if (ticketType != null) PaymentTextField(
                    label = "Numéro utilisé pour le paiement",
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = operatorPhone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .bringIntoViewRequester(phoneBringIntoView)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    phoneBringIntoView.bringIntoView()
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        }
                )

                if (ticketType != null) Spacer(Modifier.height(14.dp))

                if (ticketType != null) PaymentTextField(
                    label = "Référence de transaction",
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    placeholder = "Ex : 1234567890",
                    modifier = Modifier
                        .bringIntoViewRequester(refBringIntoView)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    refBringIntoView.bringIntoView()
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        }
                )

                if (ticketType != null) Spacer(Modifier.height(32.dp))

                if (ticketType != null) Button(
                    onClick = {
                        if (studentName.isNotBlank() && phoneNumber.isNotBlank() && transactionRef.isNotBlank()) {
                            isLoading = true
                            val paymentMethod = when (selectedOperator) {
                                MobileOperator.MVOLA -> "MVOLA"
                                MobileOperator.ORANGE_MONEY -> "ORANGE_MONEY"
                                MobileOperator.AIRTEL_MONEY -> "MTN_MOMO"
                            }
                            ordersVm.createOrder(
                                eventId = eventId,
                                paymentMethod = paymentMethod,
                                paymentReference = transactionRef.trim(),
                                ticketTypeId = ticketTypeId,
                                onSuccess = {
                                    isLoading = false
                                    onConfirm()
                                },
                                onError = {
                                    isLoading = false
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenDark,
                        contentColor = Color.White
                    ),
                    enabled = studentName.isNotBlank() && phoneNumber.isNotBlank()
                            && transactionRef.isNotBlank() && !isLoading && ticketType != null
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Confirmer le paiement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun OperatorCard(
    operator: MobileOperator,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, label) = when (operator) {
        MobileOperator.ORANGE_MONEY -> Color(0xFFFF6600) to "Orange\nMoney"
        MobileOperator.MVOLA -> Color(0xFF1B8A3E) to "MVola"
        MobileOperator.AIRTEL_MONEY -> Color(0xFFCC0000) to "Airtel\nMoney"
    }
    val dotColor = when (operator) {
        MobileOperator.ORANGE_MONEY -> Color(0xFFFFB300)
        MobileOperator.MVOLA -> Color(0xFF66BB6A)
        MobileOperator.AIRTEL_MONEY -> Color(0xFFEF9A9A)
    }

    Box(
        modifier = modifier
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .then(
                if (isSelected) Modifier.border(3.dp, SurfaceWhite, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun PaymentTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEEEEE))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
        )
        Spacer(Modifier.height(4.dp))
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
            keyboardOptions = keyboardOptions,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.titleMedium.copy(color = TextHint)
                    )
                }
                inner()
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
