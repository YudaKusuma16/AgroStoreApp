package com.apk.agrostore.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import com.apk.agrostore.presentation.common.ProductCard
import com.apk.agrostore.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.clearSearch()
        } else {
            delay(300)
            viewModel.searchProducts(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AgroStore",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.OrderHistory.route) }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Order History",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 10.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Beranda") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.HealthBot.route) },
                    icon = { Icon(Icons.Default.Chat, null) },
                    label = { Text("HealthBot") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->

        // BACKGROUND HIJAU
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Memastikan konten tidak tertutup TopBar & BottomBar
            color = MaterialTheme.colorScheme.primary
        ) {
            // CARD PUTIH DENGAN RADIUS
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp) // Padding kiri-kanan untuk Column
                ) {

                    // Jarak atas setelah lengkungan kartu putih
                    Spacer(modifier = Modifier.height(24.dp))

                    // SEARCH BAR
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari produk pertanian...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            // Memberikan padding ekstra di bagian bawah agar item terakhir bisa di-scroll melewati navbar
                            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
                        ) {
                            items(uiState.products) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = {
                                        navController.navigate(
                                            Screen.DetailProduct.createRoute(product.id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
