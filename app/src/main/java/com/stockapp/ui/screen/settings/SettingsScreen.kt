package com.stockapp.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Chip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stockapp.R
import com.stockapp.data.model.Brokerage
import com.stockapp.data.model.Language
import com.stockapp.data.model.ThemeMode
import com.stockapp.data.model.User
import com.stockapp.ui.components.CommonComponents
import com.stockapp.ui.theme.StockAppTheme
import com.stockapp.ui.theme.StockAppTheme.colorScheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.observeAsState(null)
    val users by viewModel.users.observeAsState(emptyList())
    val primaryUser by viewModel.primaryUser.observeAsState(null)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState<String?>(null)

    val showAddUserDialog = remember { mutableStateOf(false) }
    val showBrokerageDialog = remember { mutableStateOf<Brokerage?>(null) }
    val newUserName = remember { mutableStateOf("") }
    val newUserPan = remember { mutableStateOf("") }
    val newUserDemat = remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        viewModel.loadSettings()
        viewModel.loadUsers()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = "Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        }

        if (isLoading) {
            CommonComponents.LoadingState(modifier = Modifier.fillMaxSize().padding(16.dp))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Family Members Section
                item {
                    SectionHeader(title = "Family Members", actionText = "Add Member", onAction = { showAddUserDialog.value = true })
                }
                items(users) { user ->
                    item {
                        UserCard(
                            user = user,
                            isPrimary = primaryUser?.id == user.id,
                            onSetPrimary = { viewModel.setPrimaryUser(user) },
                            onEdit = { /* TODO: Edit user */ },
                            onDelete = { viewModel.deleteUser(user) }
                        )
                    }
                }

                // Brokerage Accounts Section
                item {
                    SectionHeader(title = "Brokerage Accounts", actionText = "Add Account", onAction = { showBrokerageDialog.value = Brokerage.ZERODHA })
                }
                item {
                    BrokerageAccountsCard(
                        settings = settings,
                        onBrokerageClick = { brokerage -> showBrokerageDialog.value = brokerage },
                        onDisconnect = { brokerage -> viewModel.disconnectBrokerage(brokerage) }
                    )
                }

                // App Preferences Section
                item {
                    SectionHeader(title = "App Preferences")
                }
                item {
                    PreferencesCard(
                        settings = settings,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onThemeChange = { viewModel.setThemeMode(it) },
                        onNotificationsChange = { viewModel.setNotificationsEnabled(it) },
                        onBiometricChange = { viewModel.setBiometricEnabled(it) },
                        onAutoSyncChange = { viewModel.setAutoSyncEnabled(it) },
                        onSyncIntervalChange = { viewModel.setSyncInterval(it) }
                    )
                }

                // Data & Privacy Section
                item {
                    SectionHeader(title = "Data & Privacy")
                }
                item {
                    DataPrivacyCard(
                        onExportData = { viewModel.exportData() },
                        onClearCache = { viewModel.clearCache() },
                        onDeleteAllData = { viewModel.deleteAllData() }
                    )
                }

                // About Section
                item {
                    SectionHeader(title = "About")
                }
                item {
                    AboutCard()
                }
            }
        }
    }

    // Add User Dialog
    if (showAddUserDialog.value) {
        AddUserDialog(
            onDismiss = { showAddUserDialog.value = false },
            onAdd = { name, pan, demat ->
                viewModel.addUser(name, pan, demat)
                showAddUserDialog.value = false
            }
        )
    }

    // Brokerage Dialog
    showBrokerageDialog.value?.let { brokerage ->
        BrokerageDialog(
            brokerage = brokerage,
            onDismiss = { showBrokerageDialog.value = null },
            onConnect = { apiKey, apiSecret, userId ->
                viewModel.connectBrokerage(brokerage, apiKey, apiSecret, userId)
                showBrokerageDialog.value = null
            }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface)
        actionText?.let { text ->
            onAction?.let { action ->
                Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = StockAppTheme.colorScheme.primary, modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .padding(16.dp)
                    .clickable { action() }
                )
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    isPrimary: Boolean,
    onSetPrimary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) StockAppTheme.colorScheme.primaryContainer else StockAppTheme.colorScheme.surface,
            contentColor = if (isPrimary) StockAppTheme.colorScheme.onPrimaryContainer else StockAppTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = user.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isPrimary) StockAppTheme.colorScheme.onPrimaryContainer else StockAppTheme.colorScheme.onSurface)
                    if (isPrimary) {
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(8.dp))
                        Chip(
                            onClick = {},
                            colors = androidx.compose.material3.ChipDefaults.chipColors(
                                containerColor = StockAppTheme.colorScheme.primary,
                                contentColor = StockAppTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(text = "Primary", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(text = "PAN: ${user.maskedPan} | Demat: ${user.maskedDemat}", fontSize = 12.sp, color = if (isPrimary) StockAppTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else StockAppTheme.colorScheme.onSurfaceVariant)
                user.brokerage?.let { broker ->
                    Text(text = "Broker: ${broker.value}", fontSize = 12.sp, color = if (isPrimary) StockAppTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else StockAppTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isPrimary) {
                    Button(onClick = onSetPrimary) { Text(text = "Set Primary") }
                }
                IconButton(onClick = onEdit) { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Edit", tint = if (isPrimary) StockAppTheme.colorScheme.onPrimaryContainer else StockAppTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = onDelete) { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828)) }
            }
        }
    }
}

@Composable
fun BrokerageAccountsCard(
    settings: com.stockapp.data.model.Settings?,
    onBrokerageClick: (Brokerage) -> Unit,
    onDisconnect: (Brokerage) -> Unit
) {
    val connectedBrokerages = settings?.brokerageCredentials?.keys?.toList() ?: emptyList()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Brokerage.values().forEach { brokerage ->
            val isConnected = connectedBrokerages.contains(brokerage)
            val creds = settings?.brokerageCredentials?.get(brokerage)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = StockAppTheme.colorScheme.surface,
                    contentColor = StockAppTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (brokerage) {
                                Brokerage.ZERODHA -> androidx.compose.material.icons.Icons.Default.AccountBalance
                                Brokerage.GROWW -> androidx.compose.material.icons.Icons.Default.TrendingUp
                                Brokerage.UPSTOX -> androidx.compose.material.icons.Icons.Default.ShowChart
                                Brokerage.ANGEL_ONE -> androidx.compose.material.icons.Icons.Default.Star
                            },
                            contentDescription = brokerage.value,
                            tint = if (isConnected) StockAppTheme.colorScheme.primary else StockAppTheme.colorScheme.onSurfaceVariant
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(12.dp))
                        Column {
                            Text(text = brokerage.value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = StockAppTheme.colorScheme.onSurface)
                            Text(
                                text = if (isConnected) "Connected • ${creds?.userId ?: ""}" else "Not connected",
                                fontSize = 12.sp,
                                color = if (isConnected) Color(0xFF2E7D32) else StockAppTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isConnected) {
                        Button(onClick = { onDisconnect(brokerage) }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828).copy(alpha = 0.1f), contentColor = Color(0xFFC62828))) {
                            Text(text = "Disconnect")
                        }
                    } else {
                        Button(onClick = { onBrokerageClick(brokerage) }) { Text(text = "Connect") }
                    }
                }
            }
        }
    }
}

@Composable
fun PreferencesCard(
    settings: com.stockapp.data.model.Settings?,
    onLanguageChange: (Language) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    onAutoSyncChange: (Boolean) -> Unit,
    onSyncIntervalChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Language
            SettingRow(
                title = "Language",
                subtitle = "App display language",
                trailing = {
                    androidx.compose.material3.DropdownMenu(
                        expanded = false,
                        onDismissRequest = {}
                    ) {
                        Language.values().forEach { lang ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(text = lang.value) },
                                onClick = { onLanguageChange(lang) }
                            )
                        }
                    }
                    // Simplified - using a chip for now
                    Chip(
                        onClick = { onLanguageChange(settings?.language ?: Language.ENGLISH) },
                        colors = androidx.compose.material3.ChipDefaults.chipColors(
                            containerColor = StockAppTheme.colorScheme.primaryContainer,
                            contentColor = StockAppTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(text = settings?.language?.value ?: "English", fontSize = 12.sp)
                    }
                }
            )

            // Theme
            SettingRow(
                title = "Theme",
                subtitle = "Light, Dark, or System",
                trailing = {
                    Chip(
                        onClick = { onThemeChange(settings?.themeMode ?: ThemeMode.SYSTEM) },
                        colors = androidx.compose.material3.ChipDefaults.chipColors(
                            containerColor = StockAppTheme.colorScheme.primaryContainer,
                            contentColor = StockAppTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(text = settings?.themeMode?.value ?: "System", fontSize = 12.sp)
                    }
                }
            )

            // Notifications
            SettingRow(
                title = "Notifications",
                subtitle = "Receive alerts for IPO, news, price changes",
                trailing = {
                    Switch(
                        checked = settings?.notificationsEnabled ?: true,
                        onCheckedChange = onNotificationsChange
                    )
                }
            )

            // Biometric
            SettingRow(
                title = "Biometric Lock",
                subtitle = "Use fingerprint/face to unlock app",
                trailing = {
                    Switch(
                        checked = settings?.biometricEnabled ?: false,
                        onCheckedChange = onBiometricChange
                    )
                }
            )

            // Auto Sync
            SettingRow(
                title = "Auto Sync",
                subtitle = "Automatically sync portfolio in background",
                trailing = {
                    Switch(
                        checked = settings?.autoSyncEnabled ?: true,
                        onCheckedChange = onAutoSyncChange
                    )
                }
            )

            // Sync Interval
            SettingRow(
                title = "Sync Interval",
                subtitle = "How often to sync (minutes)",
                trailing = {
                    Chip(
                        onClick = { onSyncIntervalChange((settings?.syncIntervalMinutes ?: 30) + 15) },
                        colors = androidx.compose.material3.ChipDefaults.chipColors(
                            containerColor = StockAppTheme.colorScheme.primaryContainer,
                            contentColor = StockAppTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(text = "${settings?.syncIntervalMinutes ?: 30} min", fontSize = 12.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = StockAppTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

@Composable
fun DataPrivacyCard(
    onExportData: () -> Unit,
    onClearCache: () -> Unit,
    onDeleteAllData: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DataActionRow(
                title = "Export Data",
                subtitle = "Download all your data as JSON",
                actionText = "Export",
                onAction = onExportData
            )
            DataActionRow(
                title = "Clear Cache",
                subtitle = "Remove temporary files and cached data",
                actionText = "Clear",
                onAction = onClearCache
            )
            DataActionRow(
                title = "Delete All Data",
                subtitle = "Permanently remove all app data",
                actionText = "Delete",
                actionColor = Color(0xFFC62828),
                onAction = onDeleteAllData
            )
        }
    }
}

@Composable
fun DataActionRow(
    title: String,
    subtitle: String,
    actionText: String,
    actionColor: Color = StockAppTheme.colorScheme.primary,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = StockAppTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = onAction, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = actionColor.copy(alpha = 0.1f), contentColor = actionColor)) {
            Text(text = actionText)
        }
    }
}

@Composable
fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Stock Portfolio Manager", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface)
                    Text(text = "Version 1.0.0", fontSize = 14.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                }
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Info, contentDescription = "Info", tint = StockAppTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))
            Text(text = "A personal stock market app for managing IPOs, portfolio, news, and AI insights across multiple family accounts.", fontSize = 14.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Built with Kotlin & Jetpack Compose", fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                Text(text = "Free for personal use", fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val pan = remember { mutableStateOf("") }
    val demat = remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Family Member", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.padding(16.dp).width(300.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = pan.value,
                    onValueChange = { pan.value = it },
                    label = { Text("PAN Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Text)
                )
                OutlinedTextField(
                    value = demat.value,
                    onValueChange = { demat.value = it },
                    label = { Text("Demat Account Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name.value, pan.value, demat.value) }, enabled = name.value.isNotBlank() && pan.value.isNotBlank() && demat.value.isNotBlank()) {
                Text(text = "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        }
    )
}

@Composable
fun BrokerageDialog(
    brokerage: Brokerage,
    onDismiss: () -> Unit,
    onConnect: (String, String, String) -> Unit
) {
    val apiKey = remember { mutableStateOf("") }
    val apiSecret = remember { mutableStateOf("") }
    val userId = remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Connect ${brokerage.value}", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.padding(16.dp).width(300.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = apiKey.value,
                    onValueChange = { apiKey.value = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = apiSecret.value,
                    onValueChange = { apiSecret.value = it },
                    label = { Text("API Secret") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.material3.PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = userId.value,
                    onValueChange = { userId.value = it },
                    label = { Text("User ID / Client ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConnect(apiKey.value, apiSecret.value, userId.value) }, enabled = apiKey.value.isNotBlank() && apiSecret.value.isNotBlank() && userId.value.isNotBlank()) {
                Text(text = "Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        }
    )
}