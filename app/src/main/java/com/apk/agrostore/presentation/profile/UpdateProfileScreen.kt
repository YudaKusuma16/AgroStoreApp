package com.apk.agrostore.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    navController: NavController,
    viewModel: UpdateProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle update success
    LaunchedEffect(uiState.isUpdateSuccessful) {
        if (uiState.isUpdateSuccessful) {
            // Navigate back after successful update
            navController.navigateUp()
            viewModel.clearUpdateSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil") },
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
        if (uiState.isLoading && uiState.name.isBlank()) {
            // Initial loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(60.dp)
                    )
                }

                // User Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Informasi Profil",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Role Badge
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = when (uiState.role) {
                                    "pembeli" -> "Pembeli"
                                    "penjual" -> "Penjual"
                                    else -> uiState.role
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Name Field
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("Nama Lengkap") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = uiState.error?.contains("Nama") == true,
                            supportingText = {
                                if (uiState.error?.contains("Nama") == true) {
                                    Text(
                                        text = uiState.error!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )

                        // Email Field
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            ),
                            isError = uiState.error?.contains("Email") == true || uiState.error?.contains("Format email") == true,
                            supportingText = {
                                if (uiState.error?.contains("Email") == true || uiState.error?.contains("Format email") == true) {
                                    Text(
                                        text = uiState.error!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        viewModel.saveProfile()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && uiState.name.isNotBlank() && uiState.email.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan Perubahan",
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Error Dialog
    uiState.error?.let { errorMessage ->
        if (!errorMessage.contains("Nama") && !errorMessage.contains("Email") && !errorMessage.contains("Format email")) {
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