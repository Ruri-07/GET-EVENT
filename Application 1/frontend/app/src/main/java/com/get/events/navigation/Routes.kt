package com.get.events.navigation

object Routes {
    const val SPLASH    = "splash"
    const val WELCOME   = "welcome"
    const val LOGIN     = "login"
    const val REGISTER_GRAPH = "register_graph"
    const val REGISTER1 = "register1"
    const val REGISTER2 = "register2"
    const val REGISTER3 = "register3"
    const val HOME      = "home"
    const val ADMIN_LOGIN = "admin_login"
    const val EDIT_PROFILE = "edit_profile"
    const val NOTIFICATIONS = "notifications"
    const val REGISTER_PENDING = "register_pending"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password/{email}"
    fun resetPassword(email: String) = "reset_password/$email"
}
