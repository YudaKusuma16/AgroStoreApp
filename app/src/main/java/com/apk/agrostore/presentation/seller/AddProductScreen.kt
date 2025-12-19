package com.apk.agrostore.presentation.seller

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.apk.agrostore.presentation.navigation.Screen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    parentNavController: NavController? = null,
    viewModel: AddProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val price by viewModel.price.collectAsStateWithLifecycle()
    val stock by viewModel.stock.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val imageUrl by viewModel.imageUrl.collectAsStateWithLifecycle()
    val isUploadingImage by viewModel.isUploadingImage.collectAsStateWithLifecycle()
    val selectedImagePath by viewModel.selectedImagePath.collectAsStateWithLifecycle()

    // Image Picker
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            // Convert URI to path for upload
            val path = uri.path?.let { path ->
                when(uri.scheme) {
                    "file" -> path
                    "content" -> {
                        // Handle content URI
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val tempFile =
                                File(context.cacheDir, "temp_image_${System.currentTimeMillis()}")
                            tempFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                            tempFile.absolutePath
                        }
                    }
                    else -> null
                }
            }

            if (path != null) {
                viewModel.selectAndUploadImage(path)
            }
        }
    }

    // Handle save success - show dialog instead of immediate navigation
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            // Don't navigate immediately, let user see the success dialog
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Produk") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (parentNavController != null) {
                            parentNavController.navigateUp()
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )
        }
    ) { paddingValues ->
        if (uiState.isSaving) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 55.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
            ) {
                // Image Picker Section
                Card(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                .build()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploadingImage) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Mengupload gambar...",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else if (selectedImageUri != null || imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = selectedImageUri ?: imageUrl,
                                contentDescription = "Product Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap untuk tambah foto",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Nama Produk") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { viewModel.onCategoryChange(it) },
                    label = { Text("Kategori") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price
                OutlinedTextField(
                    value = price,
                    onValueChange = { viewModel.onPriceChange(it) },
                    label = { Text("Harga") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Text(
                            text = "Rp",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stock
                OutlinedTextField(
                    value = stock,
                    onValueChange = { viewModel.onStockChange(it) },
                    label = { Text("Stok") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = { Text("Deskripsi Produk") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = { viewModel.saveProduct() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Simpan Produk",
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel Button
                OutlinedButton(
                    onClick = {
                        if (parentNavController != null) {
                            parentNavController.navigateUp()
                        } else {
                            navController.navigateUp()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Batal",
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Success Dialog
        if (uiState.isSaved) {
            AlertDialog(
                onDismissRequest = {
                    if (parentNavController != null) {
                        parentNavController.navigateUp()
                    } else {
                        navController.navigateUp()
                    }
                    viewModel.resetSaveState()
                },
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
                        text = "Berhasil!",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Text(
                        text = "Produk berhasil ditambahkan",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (parentNavController != null) {
                                parentNavController.navigateUp()
                            } else {
                                navController.navigateUp()
                            }
                            viewModel.resetSaveState()
                        }
                    ) {
                        Text("OK")
                    }
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