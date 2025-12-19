package com.apk.agrostore.presentation.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.apk.agrostore.presentation.common.toRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto refresh when entering the screen
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Transaksi Penjualan",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.transactions.size} transaksi",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Belum Ada Transaksi",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada pembelian yang dilakukan oleh pembeli",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                items(
                    items = uiState.transactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }
            }
        }

        // Error Dialog
        uiState.errorMessage?.let { errorMessage ->
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

@Composable
private fun TransactionCard(
    transaction: TransactionUiState.TransactionItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header sederhana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // ID Transaksi & Pembeli
                    Text(
                        text = "#${transaction.id}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = transaction.buyerName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = transaction.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Status Badge
                StatusBadge(status = transaction.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List Produk yang disederhanakan
            transaction.items.forEach { item ->
                ProductItemRow(item = item)
                if (transaction.items.indexOf(item) < transaction.items.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

        }
    }
}

@Composable
private fun ProductItemRow(
    item: TransactionUiState.TransactionItem.OrderItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gambar produk yang lebih kecil
        SubcomposeAsyncImage(
            model = item.productImage,
            loading = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.productName.first().toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            contentDescription = item.productName,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Informasi produk
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.quantity} × ${item.price.toRupiah()}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Subtotal
        Text(
            text = (item.price * item.quantity).toRupiah(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        "Menunggu Pembayaran" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        "Dibayar" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "Dikemas" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "Dikirim" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "Selesai" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Dibatalkan" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}