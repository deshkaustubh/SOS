package tech.kaustubhdeshpande.sos.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.kaustubhdeshpande.sos.components.SosButton
import tech.kaustubhdeshpande.sos.service.SosService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var isServiceActive by remember { mutableStateOf(false) }
    var remainingTimeSeconds by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Check if service is already running
    LaunchedEffect(Unit) {
        // You can implement service state checking here if needed
    }

    // Timer countdown effect
    LaunchedEffect(isServiceActive) {
        if (isServiceActive) {
            remainingTimeSeconds = 30 * 60 // 30 minutes
            while (isServiceActive && remainingTimeSeconds > 0) {
                delay(1000)
                remainingTimeSeconds--
            }
            if (remainingTimeSeconds <= 0) {
                isServiceActive = false
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val smsGranted = permissions[Manifest.permission.SEND_SMS] == true

        if (locationGranted && smsGranted) {
            // Start service
            val intent = Intent(context, SosService::class.java)
            context.startForegroundService(intent)
            isServiceActive = true
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Location and SMS permissions are required for SOS to work"
                )
            }
        }
    }

    // Function to check and request permissions
    fun checkAndRequestPermissions() {
        val prefs = context.getSharedPreferences("sos_prefs", android.content.Context.MODE_PRIVATE)
        val contacts = prefs.getStringSet("contacts", emptySet()) ?: emptySet()

        if (contacts.isEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar("Please add emergency contacts first")
            }
            selectedTab = 1 // Switch to contacts tab
            return
        }

        val permissionsToRequest = mutableListOf<String>()

        // Check location permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Check SMS permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // Permissions already granted, start service
            val intent = Intent(context, SosService::class.java)
            context.startForegroundService(intent)
            isServiceActive = true
        }
    }

    // Function to stop service
    fun stopService() {
        val intent = Intent(context, SosService::class.java)
        context.stopService(intent)
        isServiceActive = false
        remainingTimeSeconds = 0
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("SOS Emergency App") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("SOS") },
                    icon = { Icon(Icons.Filled.LocationOn, contentDescription = "SOS") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Contacts") },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Contacts") }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // SOS Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isServiceActive) {
                            // Timer Display
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "SOS Active",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = formatTime(remainingTimeSeconds),
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Sending location every 10 seconds",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // SOS Button
                        SosButton(
                            isActive = isServiceActive,
                            onLongPress = {
                                if (isServiceActive) {
                                    stopService()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("SOS stopped")
                                    }
                                } else {
                                    checkAndRequestPermissions()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Status message
                        if (!isServiceActive) {
                            val prefs = context.getSharedPreferences(
                                "sos_prefs",
                                android.content.Context.MODE_PRIVATE
                            )
                            val contactCount = prefs.getStringSet("contacts", emptySet())?.size ?: 0

                            Text(
                                text = if (contactCount > 0) {
                                    "$contactCount emergency contact(s) added"
                                } else {
                                    "No emergency contacts added yet"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                1 -> {
                    // Contacts Tab
                    ContactPickerScreen()
                }
            }
        }
    }
}

// Helper function to format seconds into MM:SS
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(minutes, secs)
}
