package com.gamehub.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamehub.app.ui.theme.*
import com.gamehub.app.ui.viewmodel.GameHubViewModel

@Composable
fun SettingsScreen(
    viewModel: GameHubViewModel
) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val steamUser by viewModel.steamUser.collectAsState()
    val epicUser by viewModel.epicUser.collectAsState()
    val currency by viewModel.preferredCurrency.collectAsState()
    val apiBaseUrl by viewModel.apiBaseUrl.collectAsState()
    val connectionStatus by viewModel.backendConnectionStatus.collectAsState()

    var showSteamDialog by remember { mutableStateOf(false) }
    var showEpicDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }

    var steamInput by remember { mutableStateOf("") }
    var epicInput by remember { mutableStateOf("") }
    var serverInput by remember { mutableStateOf(apiBaseUrl) }

    LaunchedEffect(apiBaseUrl) {
        serverInput = apiBaseUrl
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Settings & Accounts",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )

        // Section: Backend Gateway Configuration (Physical Phone & Emulator)
        Text(
            text = "Backend Server Connection",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "API Gateway URL",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = apiBaseUrl,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = { showServerDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit IP", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.testConnection() }) {
                            Icon(Icons.Filled.Sensors, contentDescription = "Test Connection", tint = DealGreen)
                        }
                    }
                }

                if (connectionStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (connectionStatus!!.contains("Connected", ignoreCase = true) || connectionStatus!!.contains("✓"))
                            DealGreen.copy(alpha = 0.15f)
                        else
                            DiscountRed.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = connectionStatus!!,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (connectionStatus!!.contains("Connected", ignoreCase = true) || connectionStatus!!.contains("✓"))
                                DealGreen
                            else
                                DiscountRed,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Store Account Integrations
        Text(
            text = "Store Account Integrations",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // Steam Integration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = SteamAccent, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Steam Account", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = if (steamUser != null) "Connected: $steamUser" else "Not connected",
                            fontSize = 12.sp,
                            fontWeight = if (steamUser != null) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (steamUser != null) DealGreen else TextMuted
                        )
                    }
                }

                if (steamUser != null) {
                    OutlinedButton(
                        onClick = { viewModel.disconnectStore("steam") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscountRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Disconnect", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            steamInput = ""
                            showSteamDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SteamAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Epic Games Integration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Store, contentDescription = null, tint = EpicAccent, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Epic Games Account", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = if (epicUser != null) "Connected: $epicUser" else "Not connected",
                            fontSize = 12.sp,
                            fontWeight = if (epicUser != null) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (epicUser != null) DealGreen else TextMuted
                        )
                    }
                }

                if (epicUser != null) {
                    OutlinedButton(
                        onClick = { viewModel.disconnectStore("epic") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscountRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Disconnect", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            epicInput = ""
                            showEpicDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EpicAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Display & Theme Preferences
        Text(
            text = "App Preferences",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Currency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Display Currency", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Prices converted to ₹ INR", fontSize = 11.sp, color = TextMuted)
                    }
                    Text("₹ INR", fontWeight = FontWeight.Black, color = DealGreen, fontSize = 14.sp)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Theme Toggle Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Theme Appearance",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDarkTheme) "Dark Gaming AMOLED" else "Light Clean Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) NeonCyan else Color(0xFF0070F3)
                        )
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.toggleDarkTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF0070F3)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action: Sync Library Button
        Button(
            onClick = { viewModel.loadLibrary() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sync & Refresh Entire Library", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }

    // --- Server IP Configuration Dialog ---
    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = {
                Text("Configure API Gateway", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Select a preset or enter the host URL for your PC backend:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = serverInput,
                        onValueChange = { serverInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Quick Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { serverInput = "http://127.0.0.1:8000/" },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("⚡ USB Cable (127.0.0.1:8000) [Recommended]", fontSize = 12.sp, color = NeonCyan)
                        }
                        TextButton(
                            onClick = { serverInput = "http://192.168.1.2:8000/" },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("📶 Wi-Fi LAN (192.168.1.2:8000)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        TextButton(
                            onClick = { serverInput = "http://10.0.2.2:8000/" },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("💻 Android Emulator (10.0.2.2:8000)", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (serverInput.isNotBlank()) {
                        viewModel.updateApiBaseUrl(serverInput)
                        showServerDialog = false
                    }
                }) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // --- Steam Login Dialog ---
    if (showSteamDialog) {
        AlertDialog(
            onDismissRequest = { showSteamDialog = false },
            title = {
                Text("Connect Steam Account", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Authenticate securely on Steam's official website using your Steam credentials and Steam Guard OTP:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val openIdUrl = "https://steamcommunity.com/openid/login?openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.mode=checkid_setup&openid.return_to=http%3A%2F%2F127.0.0.1%3A8000%2Fapi%2Faccounts%2Fsteam%2Fcallback&openid.realm=http%3A%2F%2F127.0.0.1%3A8000%2F&openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(openIdUrl))
                            context.startActivity(intent)
                            showSteamDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SteamAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In on Steam (Official 2FA)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "Or link your 17-digit SteamID64 directly:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = steamInput,
                        onValueChange = { steamInput = it },
                        placeholder = { Text("e.g. 76561198000000000", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val input = steamInput.trim()
                        if (input.isNotBlank()) {
                            viewModel.connectSteam(input)
                            showSteamDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SteamAccent)
                ) {
                    Text("Connect ID", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSteamDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // --- Epic Games Login Dialog ---
    if (showEpicDialog) {
        AlertDialog(
            onDismissRequest = { showEpicDialog = false },
            title = {
                Text("Connect Epic Games Account", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Enter your Epic Games Display Name or Account ID:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = epicInput,
                        onValueChange = { epicInput = it },
                        placeholder = { Text("e.g. EpicMaster_IN or gamer@epic.com", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { epicInput = "ProGamer_India" },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Use Demo Epic Account", fontSize = 11.sp, color = EpicAccent)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val input = epicInput.ifBlank { "ProGamer_India" }
                        viewModel.connectEpic(input)
                        showEpicDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EpicAccent)
                ) {
                    Text("Connect", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEpicDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
