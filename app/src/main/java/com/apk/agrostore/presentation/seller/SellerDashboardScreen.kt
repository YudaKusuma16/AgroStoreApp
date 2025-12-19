package com.apk.agrostore.presentation.seller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    navController: NavController,
    parentNavController: NavController? = null,
    viewModel: SellerViewModel = hiltViewModel(),
    showBottomBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    // Handle delete success and auto refresh on screen entry
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            viewModel.refresh()
            viewModel.resetDeleteState()
        }
    }

    // Auto refresh when entering the screen
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Tambah Produk") },
                        label = { Text("Tambah Produk") },
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            if (parentNavController != null) {
                                parentNavController.navigate(Screen.AddProduct.route)
                            } else {
                                navController.navigate(Screen.AddProduct.route)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Receipt, contentDescription = "Transaksi") },
                        label = { Text("Transaksi") },
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            if (parentNavController != null) {
                                parentNavController.navigate("transaction") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            } else {
                                navController.navigate(Screen.Transaction.route)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.products.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Belum Ada Produk",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tekan tombol + untuk menambah produk pertama Anda",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // HEADER
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "List Produk",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.products.size} Produk",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // LIST PRODUK
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(
                            bottom = 100.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(uiState.products) { product ->
                            SellerProductCard(
                                product = product,
                                onDelete = { viewModel.showDeleteConfirmation(product.id) },
                                onEdit = {
                                    navController.navigate(Screen.EditProduct.createRoute(product.id))
                                }
                            )
                        }
                    }
                }
            }

            // Floating Action Button
            if (!uiState.isLoading) {
                // When called from SellerParentScreen, FAB needs to be above the parent's bottom nav
                // Since this screen is inside a rounded surface, we need more bottom padding
                val bottomPadding = if (parentNavController != null) 110.dp else 16.dp

                FloatingActionButton(
                    onClick = {
                        if (parentNavController != null) {
                            parentNavController.navigate(Screen.AddProduct.route)
                        } else {
                            navController.navigate(Screen.AddProduct.route)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = 16.dp,
                            bottom = bottomPadding
                        ),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Product",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

            }

            // Delete Confirmation Dialog
            if (uiState.showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideDeleteConfirmation() },
                    title = { Text("Konfirmasi Hapus") },
                    text = { Text("Apakah Anda yakin ingin menghapus produk ini?") },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.confirmDeleteProduct() }
                        ) {
                            Text("Hapus", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.hideDeleteConfirmation() }
                        ) {
                            Text("Batal")
                        }
                    }
                )
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
}

@Composable
private fun SellerProductCard(
    product: com.apk.agrostore.domain.model.Product,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onEdit,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            SubcomposeAsyncImage(
                model = product.imageUrl.ifEmpty { null },
                loading = {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    ),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.price.toRupiah(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Stok: ${product.stock}",
                    fontSize = 14.sp,
                    color = if (product.stock > 0)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            // Action Buttons
            Row {
                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Product",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Product",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}