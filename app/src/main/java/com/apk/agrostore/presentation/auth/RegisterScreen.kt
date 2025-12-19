package com.apk.agrostore.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.apk.agrostore.R
import com.apk.agrostore.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val registerState by viewModel.registerState.collectAsStateWithLifecycle()

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle register state changes
    LaunchedEffect(registerState) {
        val state = registerState
        when (state) {
            is AuthState.Success -> {
                // Navigate to login on successful registration
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
                viewModel.resetRegisterState()
            }
            is AuthState.Error -> {
                showError = true
                errorMessage = state.message
            }
            else -> { /* No action needed */ }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.agrostore_logo),
                contentDescription = "AgroStore Logo",
                modifier = Modifier
                    .width(280.dp)
                    .height(280.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name TextField
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nama Lengkap") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                        if (password.length >= 6) {
                            viewModel.register(name, email, password)
                        } else {
                            showError = true
                            errorMessage = "Password minimal 6 karakter"
                        }
                    } else {
                        showError = true
                        errorMessage = "Semua field harus diisi"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = registerState !is AuthState.Loading
            ) {
                if (registerState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Daftar",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sudah punya akun?",
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                    Text(
                        text = "Login di sini",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Error Dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}