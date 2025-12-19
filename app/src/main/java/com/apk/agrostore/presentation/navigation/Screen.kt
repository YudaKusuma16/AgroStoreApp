package com.apk.agrostore.presentation.navigation

/**
 * Sealed class representing all screens in the AgroStore app.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object DetailProduct : Screen("detail_product/{productId}") {
        fun createRoute(productId: String) = "detail_product/$productId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object Profile : Screen("profile")
    object UpdateProfile : Screen("update_profile")
    object OrderHistory : Screen("order_history")
    object SellerDashboard : Screen("seller_dashboard")
    object AddProduct : Screen("add_product")
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: String) = "edit_product/$productId"
    }
    object Transaction : Screen("transaction")
    object HealthBot : Screen("healthbot")
}