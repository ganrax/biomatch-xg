package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.MainViewModel
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.ChatbotScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LiveHalfScreen
import com.example.ui.screens.PreMatchScreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepVoid
import com.example.ui.theme.KineticEmerald
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class AppTab(val title: String) {
    PRE_MATCH("Pre-Match"),
    LIVE_1H("Élő 1H"),
    CHAT("AI Konzultáció"),
    ARCHIVE("Archívum")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BioMatchApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BioMatchApp(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.PRE_MATCH) }
    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage by viewModel.statusMessage.collectAsState()

    val updateInfo by viewModel.updateInfo.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }

    // Auto-check for updates quietly on first launch
    LaunchedEffect(Unit) {
        viewModel.checkForAppUpdates(isManual = false)
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            initialKey = viewModel.getCustomApiKey(),
            onSaveKey = { viewModel.saveCustomApiKey(it) },
            onDismiss = { showApiKeyDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepVoid,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BioMatch xG",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(DeepVoid, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Build #${BuildConfig.BUILD_NUMBER} (${BuildConfig.BUILD_TIME_STR})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showApiKeyDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "AI és OCR Beállítások",
                            tint = if (viewModel.isApiKeyConfigured()) KineticEmerald else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    if (isCheckingUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = NeonCyan
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.checkForAppUpdates(isManual = true) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Frissítés keresése",
                                tint = if (updateInfo?.hasUpdate == true) KineticEmerald else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.PRE_MATCH,
                    onClick = { currentTab = AppTab.PRE_MATCH },
                    icon = { Icon(Icons.Default.Science, contentDescription = "Pre-Match") },
                    label = { Text(AppTab.PRE_MATCH.title) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepVoid,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_pre_match")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.LIVE_1H,
                    onClick = { currentTab = AppTab.LIVE_1H },
                    icon = { Icon(Icons.Default.Speed, contentDescription = "Élő 1H") },
                    label = { Text(AppTab.LIVE_1H.title) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepVoid,
                        selectedTextColor = KineticEmerald,
                        indicatorColor = KineticEmerald,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_live_1h")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CHAT,
                    onClick = { currentTab = AppTab.CHAT },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "AI Chatbot") },
                    label = { Text(AppTab.CHAT.title) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepVoid,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_chat")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.ARCHIVE,
                    onClick = { currentTab = AppTab.ARCHIVE },
                    icon = { Icon(Icons.Default.History, contentDescription = "Archívum") },
                    label = { Text(AppTab.ARCHIVE.title) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepVoid,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_archive")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.PRE_MATCH -> PreMatchScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.LIVE_1H -> LiveHalfScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.CHAT -> ChatbotScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.ARCHIVE -> HistoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }

        updateInfo?.let { info ->
            UpdateDialog(
                updateInfo = info,
                downloadProgress = downloadProgress,
                currentRepo = viewModel.githubRepoSlug,
                onSaveRepo = { newRepo -> viewModel.githubRepoSlug = newRepo },
                onStartDownload = { url -> viewModel.downloadAndInstallUpdate(url) },
                onDismiss = { viewModel.dismissUpdateDialog(info.latestVersionName) }
            )
        }
    }
}

