package com.uzuu.customer.feature.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Login : Routes("login")
    object Register : Routes("register")
    object ForgotPassword : Routes("forgot_password")
    object Main : Routes("main")

    // Tabs inside Main
    object BlogTab : Routes("blog_tab")
    object EventTab : Routes("event_tab")
    object CartTab : Routes("cart_tab")
    object TicketTab : Routes("ticket_tab")
    object PersonalTab : Routes("personal_tab")

    // Inner / Detail screens
    object EventDetail : Routes("event_detail")
    object Checkout : Routes("checkout")
    object VoucherList : Routes("voucher_list")
    object PersonalInfo : Routes("personal_info")
    object ChangePassword : Routes("change_password")
    object History : Routes("history")
}
