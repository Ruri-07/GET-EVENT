package com.get.events.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.get.events.ui.screens.PendingTicketInfo
import com.get.events.data.repository.TicketTypeHelper
import com.get.events.viewmodel.EventsViewModel
import com.get.events.viewmodel.OrdersViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.get.events.ui.screens.*
import com.get.events.ui.screens.auth.*
import com.get.events.ui.screens.auth.RegisterPendingScreen
import com.get.events.navigation.admin.AdminRoutes
import com.get.events.navigation.admin.AdminApp

sealed class Screen(val route: String) {
    object Home   : Screen("home")
    object Events : Screen("events")
    object MyTickets : Screen("my_tickets")
    object Profile   : Screen("profile")

    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object ContestRegister : Screen("contest_register/{eventId}") {
        fun createRoute(eventId: String) = "contest_register/$eventId"
    }
    object TicketDetail : Screen("ticket_detail/{ticketId}") {
        fun createRoute(ticketId: String) = "ticket_detail/$ticketId"
    }
    object TicketType : Screen("ticket_type/{eventId}") {
        fun createRoute(eventId: String) = "ticket_type/$eventId"
    }
    object MobilePayment : Screen("mobile_payment/{eventId}/{ticketTypeId}") {
        fun createRoute(eventId: String, ticketTypeId: String) =
            "mobile_payment/$eventId/$ticketTypeId"
    }
    object PaymentPending : Screen("payment_pending/{eventId}?isContest={isContest}") {
        fun createRoute(eventId: String, isContest: Boolean = false) =
            "payment_pending/$eventId?isContest=$isContest"
    }
    object PaymentConfirmation : Screen("payment_confirmation/{ticketId}") {
        fun createRoute(ticketId: String) = "payment_confirmation/$ticketId"
    }
}

val bottomBarRoutes = listOf(
    Screen.Home.route,
    Screen.Events.route,
    Screen.MyTickets.route,
    Screen.Profile.route
)

@Composable
fun GetEventsNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.SPLASH,
        modifier         = modifier
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(navController)
        }
        composable(Routes.WELCOME) {
            WelcomeScreen(navController)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController)
        }
        composable(
            route = Routes.RESET_PASSWORD,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(email = email, navController = navController)
        }
        navigation(
            route = Routes.REGISTER_GRAPH,
            startDestination = Routes.REGISTER1
        ) {
            composable(Routes.REGISTER1) {
                RegisterStep1Screen(navController)
            }
            composable(Routes.REGISTER2) {
                RegisterStep2Screen(navController)
            }
            composable(Routes.REGISTER3) {
                RegisterStep3Screen(navController)
            }
        }
        composable(Routes.REGISTER_PENDING) {
            RegisterPendingScreen(navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToNotifications = {
                    navController.navigate(Routes.NOTIFICATIONS)
                }
            )
        }

        composable(Screen.Events.route) {
            EventsScreen(
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                }
            )
        }

        composable(Screen.MyTickets.route) {
            MyTicketsScreen(
                onTicketClick = { ticketId ->
                    navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(navController)
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(navController)
        }
        composable(
            route     = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId           = eventId,
                onBack            = { navController.popBackStack() },
                onBuyTicket       = { navController.navigate(Screen.TicketType.createRoute(eventId)) },
                onContestRegister = { contestEventId ->
                    navController.navigate(Screen.ContestRegister.createRoute(contestEventId))
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(
            route     = Screen.ContestRegister.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            ContestRegisterScreen(
                eventId   = eventId,
                onBack    = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.PaymentPending.createRoute(eventId, isContest = true)) {
                        popUpTo(Screen.EventDetail.route)
                    }
                }
            )
        }

        composable(
            route     = Screen.TicketDetail.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: return@composable
            TicketDetailScreen(
                ticketId = ticketId,
                onBack   = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.TicketType.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            TicketTypeScreen(
                eventId    = eventId,
                onBack     = { navController.popBackStack() },
                onContinue = { ticketTypeId ->
                    navController.navigate(Screen.MobilePayment.createRoute(eventId, ticketTypeId))
                }
            )
        }

        composable(
            route     = Screen.MobilePayment.route,
            arguments = listOf(
                navArgument("eventId")      { type = NavType.StringType },
                navArgument("ticketTypeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId      = backStackEntry.arguments?.getString("eventId") ?: return@composable
            val ticketTypeId = backStackEntry.arguments?.getString("ticketTypeId") ?: return@composable
            MobilePaymentScreen(
                eventId      = eventId,
                ticketTypeId = ticketTypeId,
                onBack       = { navController.popBackStack() },
                onConfirm    = {
                    navController.navigate(Screen.PaymentPending.createRoute(eventId)) {
                        popUpTo(Screen.EventDetail.route)
                    }
                }
            )
        }

        composable(
            route     = Screen.PaymentPending.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("isContest") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            val isContest = backStackEntry.arguments?.getBoolean("isContest") ?: false
            val eventsVm: EventsViewModel = viewModel()
            val ordersVm: OrdersViewModel = viewModel()
            LaunchedEffect(eventId) {
                eventsVm.loadEvent(eventId)
                ordersVm.loadOrders()
            }
            val event by eventsVm.selectedEvent.collectAsState()
            val orders by ordersVm.orders.collectAsState()
            val latestOrder = orders
                .filter { it.eventId.toString() == eventId }
                .maxByOrNull { it.createdAt }
            val amountLabel = latestOrder?.let { order ->
                if (order.totalAmount > 0) TicketTypeHelper.formatPriceAr(order.totalAmount.toInt()) else null
            }
            val pendingInfo = PendingTicketInfo(
                eventOrganizer = "GET · Télécommunications",
                eventTitle     = event?.title ?: "Événement GET",
                eventDateLabel = event?.dateLabel ?: "Date à confirmer",
                isContest      = isContest || event?.category.equals("CONCOURS", ignoreCase = true),
                amountLabel    = amountLabel
            )
            PaymentPendingScreen(
                ticketInfo = pendingInfo,
                onBack     = {
                    navController.navigate(Screen.MyTickets.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route     = Screen.PaymentConfirmation.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: return@composable
            TicketDetailScreen(
                ticketId = ticketId,
                onBack   = {
                    navController.navigate(Screen.MyTickets.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(AdminRoutes.LOGIN) {
            AdminApp(
                onExitAdmin = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
