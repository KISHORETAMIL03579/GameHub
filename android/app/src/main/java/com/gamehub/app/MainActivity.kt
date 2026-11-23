package com.gamehub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamehub.app.data.local.GameHubDatabase
import com.gamehub.app.data.local.PreferencesManager
import com.gamehub.app.data.repository.GameHubRepository
import com.gamehub.app.ui.navigation.Screen
import com.gamehub.app.ui.screens.*
import com.gamehub.app.ui.theme.GameHubTheme
import com.gamehub.app.ui.theme.NeonCyan
import com.gamehub.app.ui.theme.SurfaceDark
import com.gamehub.app.ui.theme.TextMuted
import com.gamehub.app.ui.viewmodel.GameHubViewModel
import kotlinx.coroutines.launch

import android.content.Intent

class MainActivity : ComponentActivity() {
    private var viewModelRef: GameHubViewModel? = null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthIntent(intent)
    }

    private fun handleAuthIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "gamehub" && data.host == "auth") {
            val steamId = data.getQueryParameter("steam_id")
            if (!steamId.isNullOrBlank()) {
                viewModelRef?.connectSteam(steamId)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize local persistence & repository
        val database = GameHubDatabase.getInstance(applicationContext)
        val preferences = PreferencesManager(applicationContext)
        val repository = GameHubRepository(database, preferences)

        setContent {
            val viewModel: GameHubViewModel = remember {
                GameHubViewModel(repository).also { viewModelRef = it }
            }
            LaunchedEffect(Unit) {
                handleAuthIntent(intent)
            }
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            GameHubTheme(darkTheme = isDarkTheme) {
                val coroutineScope = rememberCoroutineScope()
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { Screen.bottomNavItems.size }
                )

                var selectedGameIdForDetails by remember { mutableStateOf<String?>(null) }
                var libraryTabIndex by remember { mutableStateOf(0) }
                var activeSubScreen by remember { mutableStateOf<SubScreen?>(null) }

                BackHandler(enabled = activeSubScreen != null || selectedGameIdForDetails != null) {
                    if (selectedGameIdForDetails != null) {
                        selectedGameIdForDetails = null
                        viewModel.clearSelectedGame()
                    } else if (activeSubScreen != null) {
                        activeSubScreen = null
                    }
                }

                if (activeSubScreen != null) {
                    when (activeSubScreen) {
                        SubScreen.Notifications -> {
                            NotificationsScreen(
                                viewModel = viewModel,
                                onBack = { activeSubScreen = null },
                                onGameSelected = { gameId, title, cover, store, price, stores ->
                                    selectedGameIdForDetails = gameId
                                    viewModel.selectGame(gameId, title, cover, store, price, stores)
                                }
                            )
                        }
                        SubScreen.Profile -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                onBack = { activeSubScreen = null }
                            )
                        }
                        SubScreen.AllDeals -> {
                            AllDealsScreen(
                                viewModel = viewModel,
                                onBack = { activeSubScreen = null },
                                onGameSelected = { gameId, title, cover, store, price, stores ->
                                    selectedGameIdForDetails = gameId
                                    viewModel.selectGame(gameId, title, cover, store, price, stores)
                                }
                            )
                        }
                        SubScreen.RecentlyAdded -> {
                            RecentlyAddedScreen(
                                viewModel = viewModel,
                                onBack = { activeSubScreen = null },
                                onGameSelected = { gameId, title, cover, store, price, stores ->
                                    selectedGameIdForDetails = gameId
                                    viewModel.selectGame(gameId, title, cover, store, price, stores)
                                }
                            )
                        }
                        null -> {}
                    }
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.background,
                                tonalElevation = 0.dp
                            ) {
                                Screen.bottomNavItems.forEachIndexed { index, screen ->
                                    val selected = pagerState.currentPage == index
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF6C5CE7),
                                            selectedTextColor = Color(0xFF6C5CE7),
                                            unselectedIconColor = TextMuted,
                                            unselectedTextColor = TextMuted,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        // Full-screen horizontal swipe between all 5 main tabs
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) { pageIndex ->
                            when (pageIndex) {
                                0 -> HomeScreen(
                                    viewModel = viewModel,
                                    onGameSelected = { gameId, title, cover, store, price, stores ->
                                        selectedGameIdForDetails = gameId
                                        viewModel.selectGame(
                                            gameId = gameId,
                                            initialTitle = title,
                                            initialCover = cover,
                                            initialStore = store,
                                            initialPrice = price,
                                            initialStores = stores
                                        )
                                    },
                                    onNavigateToLibrary = { tab ->
                                        libraryTabIndex = tab
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(1)
                                        }
                                    },
                                    onNavigateToNotifications = { activeSubScreen = SubScreen.Notifications },
                                    onNavigateToProfile = { activeSubScreen = SubScreen.Profile },
                                    onNavigateToAllDeals = { activeSubScreen = SubScreen.AllDeals },
                                    onNavigateToRecentlyAdded = { activeSubScreen = SubScreen.RecentlyAdded },
                                    onNavigateToExplore = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(3)
                                        }
                                    }
                                )
                                1 -> LibraryScreen(
                                    viewModel = viewModel,
                                    initialTab = libraryTabIndex,
                                    onGameSelected = { gameId ->
                                        selectedGameIdForDetails = gameId
                                        viewModel.selectGame(gameId)
                                    }
                                )
                                2 -> WishlistScreen(
                                    viewModel = viewModel,
                                    onGameSelected = { gameId ->
                                        selectedGameIdForDetails = gameId
                                        viewModel.selectGame(gameId)
                                    }
                                )
                                3 -> OffersScreen(viewModel = viewModel)
                                4 -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                // Bottom Sheet for Game Details & Price Comparison (can be opened from any screen/subscreen)
                if (selectedGameIdForDetails != null) {
                    GameDetailsSheet(
                        viewModel = viewModel,
                        onDismiss = {
                            selectedGameIdForDetails = null
                            viewModel.clearSelectedGame()
                        }
                    )
                }
            }
        }
    }
}

sealed interface SubScreen {
    data object Notifications : SubScreen
    data object Profile : SubScreen
    data object AllDeals : SubScreen
    data object RecentlyAdded : SubScreen
}
