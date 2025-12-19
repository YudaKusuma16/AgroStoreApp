package com.apk.agrostore.presentation.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.apk.agrostore.presentation.common.toRupiah
import com.apk.agrostore.presentation.navigation.Screen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle payment success
    LaunchedEffect(uiState.isPaymentSuccessful) {
        if (uiState.isPaymentSuccessful) {
            // Show success dialog for 3 seconds then navigate to home
            delay(3000)
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Cart.route) {
                    inclusive = true
                }
            }
            viewModel.resetPaymentState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isProcessing) {
            // Payment Processing Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Memproses Pembayaran...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Order Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Ringkasan Pesanan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // List of items in cart
                            uiState.cartItems.forEach { cartItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${cartItem.quantity}x",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(40.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cartItem.product.name,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = cartItem.getTotalPrice().toRupiah(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            HorizontalDivider()

                            Spacer(modifier = Modifier.height(12.dp))

                            // Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Pembayaran",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = uiState.totalAmount.toRupiah(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Shipping Address
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Alamat Pengiriman",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = uiState.shippingAddress,
                                onValueChange = { viewModel.updateAddress(it) },
                                label = { Text("Masukkan alamat lengkap") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Jl. Contoh No. 123, Jakarta") },
                                maxLines = 3
                            )
                        }
                    }
                }

                // Payment Method
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Metode Pembayaran",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Payment Method Selection
                            uiState.availablePaymentMethods.forEach { method ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = uiState.paymentMethod == method,
                                            onClick = { viewModel.updatePaymentMethod(method) }
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = uiState.paymentMethod == method,
                                        onClick = { viewModel.updatePaymentMethod(method) }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = method,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Payment Button
                item {
                    Button(
                        onClick = {
                            viewModel.processPayment()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isProcessing && uiState.shippingAddress.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bayar Sekarang",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Payment Success Dialog
        if (uiState.isPaymentSuccessful) {
            AlertDialog(
                onDismissRequest = { /* Do nothing, auto-dismiss after delay */ },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Pembayaran Berhasil!",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Text(
                        text = "Terima kasih. Pesanan Anda sedang diproses.",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    // Empty button to prevent manual dismiss
                }
            )
        }


        // Error Dialog
        uiState.error?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}