package com.apk.agrostore.presentation.seller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apk.agrostore.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerParentScreen(
    navController: NavController
) {
    val innerNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard Penjual",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        innerNavController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Inventory, contentDescription = null)
                    },
                    label = { Text("Produk") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        innerNavController.navigate("transaction") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                    },
                    label = { Text("Transaksi") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )
            }
        }

    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            color = MaterialTheme.colorScheme.primary
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp
                ),
                color = MaterialTheme.colorScheme.background
            ) {
                NavHost(
                    navController = innerNavController,
                    startDestination = "dashboard"
                ) {
                    composable("dashboard") {
                        SellerDashboardScreen(
                            navController = navController,
                            parentNavController = innerNavController,
                            showBottomBar = false
                        )
                    }
                    composable("transaction") {
                        TransactionScreen(navController = navController)
                    }
                    composable(Screen.AddProduct.route) {
                        AddProductScreen(
                            navController = navController,
                            parentNavController = innerNavController
                        )
                    }
                    composable(
                        route = Screen.EditProduct.route,
                        arguments = listOf(
                            navArgument("productId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        EditProductScreen(
                            navController = navController,
                            parentNavController = innerNavController
                        )
                    }
                }
            }
        }
    }
    }