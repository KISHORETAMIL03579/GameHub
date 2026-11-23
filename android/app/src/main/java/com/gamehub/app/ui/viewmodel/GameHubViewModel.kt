package com.gamehub.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamehub.app.data.local.entity.RecentSearchEntity
import com.gamehub.app.data.local.entity.WishlistEntity
import com.gamehub.app.data.remote.model.*
import com.gamehub.app.data.repository.GameHubRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data class Success(val games: List<GameSummary>) : SearchState
    data class Error(val message: String) : SearchState
}

enum class StoreFilter(val label: String, val apiValue: String) {
    ALL("All Stores", "all"),
    STEAM("Steam", "steam"),
    EPIC("Epic Games", "epic")
}

@OptIn(FlowPreview::class)
class GameHubViewModel(
    private val repository: GameHubRepository
) : ViewModel() {

    // --- Search & Store Filter State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState = _searchState.asStateFlow()

    private val _selectedStoreFilter = MutableStateFlow(StoreFilter.ALL)
    val selectedStoreFilter = _selectedStoreFilter.asStateFlow()

    private val _trendingGames = MutableStateFlow<List<GameSummary>>(emptyList())
    val trendingGames = _trendingGames.asStateFlow()

    private val _dashboardData = MutableStateFlow<DashboardResponse?>(null)
    val dashboardData = _dashboardData.asStateFlow()

    private val _isLoadingDashboard = MutableStateFlow(false)
    val isLoadingDashboard = _isLoadingDashboard.asStateFlow()

    private var searchJob: Job? = null

    val recentSearches: StateFlow<List<RecentSearchEntity>> = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Game Details & Comparison State ---
    private val _selectedGameDetails = MutableStateFlow<GameDetails?>(null)
    val selectedGameDetails = _selectedGameDetails.asStateFlow()

    private val _priceComparison = MutableStateFlow<PriceComparisonResponse?>(null)
    val priceComparison = _priceComparison.asStateFlow()

    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails = _isLoadingDetails.asStateFlow()

    private val _detailsError = MutableStateFlow<String?>(null)
    val detailsError = _detailsError.asStateFlow()

    // --- Offers & Free Games State ---
    private val _freeGames = MutableStateFlow<List<FreeGameOffer>>(emptyList())
    val freeGames = _freeGames.asStateFlow()

    private val _isLoadingOffers = MutableStateFlow(false)
    val isLoadingOffers = _isLoadingOffers.asStateFlow()

    // --- Unified Library State ---
    private val _libraryData = MutableStateFlow<UnifiedLibraryResponse?>(null)
    val libraryData = _libraryData.asStateFlow()

    private val _isLoadingLibrary = MutableStateFlow(false)
    val isLoadingLibrary = _isLoadingLibrary.asStateFlow()

    // --- Wishlist State ---
    val wishlist: StateFlow<List<WishlistEntity>> = repository.getWishlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Settings & Accounts ---
    val isDarkTheme = repository.preferences.darkThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val steamUser = repository.preferences.steamUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val epicUser = repository.preferences.epicUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val preferredCurrency = repository.preferences.currencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "INR")

    val apiBaseUrl = repository.preferences.apiBaseUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "http://127.0.0.1:8000/")

    // --- Notifications State ---
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _backendConnectionStatus = MutableStateFlow<String?>(null)
    val backendConnectionStatus = _backendConnectionStatus.asStateFlow()

    init {
        viewModelScope.launch {
            val savedUrl = repository.getActiveBaseUrl()
            repository.updateBaseUrl(savedUrl)
            testConnection()
            loadDashboard()
            loadFreeGames()
            loadLibrary()
            loadNotifications()
        }

        // Live search with 400ms debounce
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.trim().length >= 2) {
                        performSearch(query.trim(), _selectedStoreFilter.value)
                    } else if (query.isBlank()) {
                        _searchState.value = SearchState.Idle
                    }
                }
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoadingDashboard.value = true
            repository.getDashboard()
                .onSuccess { data ->
                    _dashboardData.value = data
                }
            _isLoadingDashboard.value = false
        }
    }

    private fun loadInitialTrending() {
        viewModelScope.launch {
            repository.searchGames("Cyberpunk", "all")
                .onSuccess { results ->
                    _trendingGames.value = results
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
        searchJob?.cancel()
        _searchState.value = SearchState.Idle
    }

    fun setStoreFilter(filter: StoreFilter) {
        _selectedStoreFilter.value = filter
        val currentQuery = _searchQuery.value.trim()
        if (currentQuery.length >= 2) {
            performSearch(currentQuery, filter)
        }
    }

    fun searchImmediately(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        _searchQuery.value = clean
        performSearch(clean, _selectedStoreFilter.value)
    }

    private fun performSearch(query: String, filter: StoreFilter) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.value = SearchState.Loading
            repository.searchGames(query, filter.apiValue)
                .onSuccess { results ->
                    _searchState.value = SearchState.Success(results)
                }
                .onFailure { error ->
                    _searchState.value = SearchState.Error(error.localizedMessage ?: "Failed to find games")
                }
        }
    }

    fun toggleDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.preferences.setDarkTheme(isDark)
        }
    }

    fun updateApiBaseUrl(url: String) {
        viewModelScope.launch {
            repository.updateBaseUrl(url)
            testConnection()
            loadFreeGames()
            loadLibrary()
            loadInitialTrending()
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _backendConnectionStatus.value = "Pinging server..."
            repository.pingBackend()
                .onSuccess {
                    _backendConnectionStatus.value = "Connected Successfully ✓"
                }
                .onFailure { err ->
                    _backendConnectionStatus.value = "Connection Failed: ${err.localizedMessage ?: "Unreachable"}"
                }
        }
    }

    fun selectGame(
        gameId: String,
        initialTitle: String? = null,
        initialCover: String? = null,
        initialStore: StoreType? = null,
        initialPrice: String? = null,
        initialStores: List<StorePriceInfo> = emptyList()
    ) {
        _detailsError.value = null
        // 1. Immediately provide optimistic game details so the bottom sheet opens with 0ms delay!
        if (initialTitle != null) {
            val fallbackStoreInfo = if (initialStore != null) {
                listOf(
                    StorePriceInfo(
                        store = initialStore,
                        storeItemId = gameId,
                        storeUrl = if (initialStore == StoreType.STEAM) "https://store.steampowered.com" else "https://store.epicgames.com",
                        price = 0.0,
                        originalPrice = 0.0,
                        formattedPrice = initialPrice ?: "Live price",
                        formattedOriginalPrice = ""
                    )
                )
            } else emptyList()

            _selectedGameDetails.value = GameDetails(
                gameId = gameId,
                title = initialTitle,
                normalizedTitle = initialTitle.lowercase(),
                description = "Loading game description and live store comparison...",
                shortDescription = "Live PC gaming edition.",
                coverImage = initialCover,
                bannerImage = initialCover,
                stores = initialStores.ifEmpty { fallbackStoreInfo }
            )
        }

        // 2. Concurrently fetch full details and price comparison in background
        viewModelScope.launch {
            _isLoadingDetails.value = true
            val detailsJob = async { repository.getGameDetails(gameId, initialTitle) }
            val priceJob = async { repository.comparePrices(gameId, initialTitle) }

            val detailsRes = detailsJob.await()
            val priceRes = priceJob.await()

            detailsRes.onSuccess { details ->
                _selectedGameDetails.value = details
            }.onFailure { err ->
                if (_selectedGameDetails.value == null) {
                    _detailsError.value = err.localizedMessage ?: "Failed to load game details."
                }
            }

            priceRes.onSuccess { comp ->
                _priceComparison.value = comp
            }

            _isLoadingDetails.value = false
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            repository.getNotifications()
                .onSuccess { list ->
                    _notifications.value = list
                }
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun retrySelectedGame(gameId: String) {
        selectGame(gameId)
    }

    fun clearSelectedGame() {
        _selectedGameDetails.value = null
        _priceComparison.value = null
        _detailsError.value = null
    }

    fun toggleWishlist(game: GameSummary) {
        viewModelScope.launch {
            val exists = wishlist.value.any { it.gameId == game.gameId }
            if (exists) {
                repository.removeFromWishlist(game.gameId)
            } else {
                repository.addToWishlist(game)
            }
        }
    }

    fun removeFromWishlist(gameId: String) {
        viewModelScope.launch {
            repository.removeFromWishlist(gameId)
        }
    }

    fun loadFreeGames() {
        viewModelScope.launch {
            _isLoadingOffers.value = true
            repository.getFreeGames()
                .onSuccess { offers ->
                    _freeGames.value = offers
                }
            _isLoadingOffers.value = false
        }
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _isLoadingLibrary.value = true
            repository.getUnifiedLibrary()
                .onSuccess { response ->
                    _libraryData.value = response
                }
            _isLoadingLibrary.value = false
        }
    }

    fun connectSteam(steamId: String) {
        viewModelScope.launch {
            val cleanId = steamId.trim()
            if (cleanId.isBlank()) return@launch
            repository.preferences.setSteamUser(cleanId)
            _backendConnectionStatus.value = "Connecting Steam: $cleanId..."

            repository.connectSteam(cleanId, "SteamUser_$cleanId")
                .onSuccess {
                    _backendConnectionStatus.value = "Steam connected successfully ✓"
                    loadLibrary()
                }
                .onFailure { err ->
                    _backendConnectionStatus.value = "Steam saved locally (Server unreachable: ${err.localizedMessage ?: "Offline"})"
                    loadLibrary()
                }
        }
    }

    fun connectEpic(epicId: String) {
        viewModelScope.launch {
            val cleanId = epicId.trim()
            if (cleanId.isBlank()) return@launch
            repository.preferences.setEpicUser(cleanId)
            _backendConnectionStatus.value = "Connecting Epic Games: $cleanId..."

            repository.connectEpic(cleanId, "EpicGamer_$cleanId")
                .onSuccess {
                    _backendConnectionStatus.value = "Epic Games connected successfully ✓"
                    loadLibrary()
                }
                .onFailure { err ->
                    _backendConnectionStatus.value = "Epic saved locally (Server unreachable: ${err.localizedMessage ?: "Offline"})"
                    loadLibrary()
                }
        }
    }

    fun disconnectStore(store: String) {
        viewModelScope.launch {
            if (store.equals("steam", ignoreCase = true)) {
                repository.preferences.setSteamUser(null)
            } else if (store.equals("epic", ignoreCase = true)) {
                repository.preferences.setEpicUser(null)
            }
            repository.disconnectStore(store)
            _backendConnectionStatus.value = "$store disconnected"
            loadLibrary()
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            repository.deleteRecentSearch(query)
        }
    }
}
