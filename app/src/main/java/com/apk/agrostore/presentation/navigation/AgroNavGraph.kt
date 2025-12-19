package com.apk.agrostore.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.apk.agrostore.presentation.auth.LoginScreen
import com.apk.agrostore.presentation.auth.RegisterScreen
import com.apk.agrostore.presentation.cart.CartScreen
import com.apk.agrostore.presentation.checkout.CheckoutScreen
import com.apk.agrostore.presentation.detail.DetailScreen
import com.apk.agrostore.presentation.home.HomeScreen
import com.apk.agrostore.presentation.order.OrderHistoryScreen
import com.apk.agrostore.presentation.profile.ProfileScreen
import com.apk.agrostore.presentation.profile.UpdateProfileScreen
import com.apk.agrostore.presentation.seller.AddProductScreen
import com.apk.agrostore.presentation.seller.EditProductScreen
import com.apk.agrostore.presentation.seller.SellerDashboardScreen
import com.apk.agrostore.presentation.seller.SellerParentScreen
import com.apk.agrostore.presentation.healthbot.HealthBotScreen

@Composable
fun AgroNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.DetailProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            DetailScreen(navController = navController)
        }

        composable(Screen.SellerDashboard.route) {
            SellerParentScreen(navController = navController)
        }

        composable(Screen.AddProduct.route) {
            AddProductScreen(navController = navController)
        }

        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            EditProductScreen(navController = navController)
        }

        composable(Screen.Cart.route) {
            CartScreen(navController = navController)
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.UpdateProfile.route) {
            UpdateProfileScreen(navController = navController)
        }

        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(navController = navController)
        }

        composable(Screen.HealthBot.route) {
            HealthBotScreen(navController = navController)
        }
    }
}