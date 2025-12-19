package com.apk.agrostore.presentation.healthbot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apk.agrostore.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.apk.agrostore.presentation.healthbot.components.EmergencyBanner
import com.apk.agrostore.presentation.healthbot.components.MessageBubble
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthBotScreen(
    navController: NavController,
    viewModel: HealthBotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Auto scroll to bottom when new message added
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "HealthBot",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Asisten Kesehatan Anda",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Emergency banner if needed
            uiState.currentPrediction?.let { prediction ->
                if (prediction.emergencyLevel != EmergencyLevel.LOW) {
                    EmergencyBanner(
                        emergencyLevel = prediction.emergencyLevel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Messages list
            if (uiState.messages.isEmpty()) {
                // Welcome screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.doctor),
                            contentDescription = "HealthBot Doctor",
                            modifier = Modifier
                                .size(170.dp)
                                .padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Selamat Datang di HealthBot!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Saya adalah asisten kesehatan berbasis AI yang siap membantu mengidentifikasi gejala kesehatan Anda.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Contoh pertanyaan:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                listOf(
                                    "Saya demam tinggi",
                                    "Perut saya mules dan diare",
                                    "Saya sering haus dan kencing manis",
                                    "Kulit saya gatal-gatal"
                                ).forEach { example ->
                                    Text(
                                        text = "• $example",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageBubble(message = message)
                    }
                }
            }

            // Input field
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val errorMessage = uiState.error
                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = errorMessage,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = { viewModel.updateInputText(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ketik gejala Anda...") },
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )

                        FloatingActionButton(
                            onClick = { viewModel.sendMessage() },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = "Disclaimer: Informasi ini bukan pengganti diagnosis medis. Untuk diagnosis akurat, silakan konsultasi dengan dokter.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}