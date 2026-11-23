package com.gamehub.app.data.repository

import com.gamehub.app.data.local.GameHubDatabase
import com.gamehub.app.data.local.PreferencesManager
import com.gamehub.app.data.local.entity.RecentSearchEntity
import com.gamehub.app.data.local.entity.WishlistEntity
import com.gamehub.app.data.remote.ApiClient
import com.gamehub.app.data.remote.api.GameHubApiService
import com.gamehub.app.data.remote.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GameHubRepository(
    private val database: GameHubDatabase,
    val preferences: PreferencesManager
) {
    private val wishlistDao = database.wishlistDao()
    private val recentSearchDao = database.recentSearchDao()
    private var apiService: GameHubApiService = ApiClient.create()

    suspend fun updateBaseUrl(newUrl: String) {
        preferences.setApiBaseUrl(newUrl)
        apiService = ApiClient.create(newUrl)
    }

    suspend fun getActiveBaseUrl(): String {
        return preferences.apiBaseUrlFlow.first()
    }

    suspend fun pingBackend(): Result<Boolean> {
        return try {
            // Test with a lightweight query
            val results = apiService.getFreeGames()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remote Searches
    suspend fun searchGames(query: String, store: String? = null): Result<List<GameSummary>> {
        return try {
            if (query.isNotBlank()) {
                recentSearchDao.insert(RecentSearchEntity(query = query))
            }
            val results = apiService.searchGames(query, store = store)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameDetails(gameId: String, title: String? = null): Result<GameDetails> {
        return try {
            val details = apiService.getGameDetails(gameId, title)
            Result.success(details)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun comparePrices(gameId: String, title: String? = null): Result<PriceComparisonResponse> {
        return try {
            val comp = apiService.comparePrices(gameId, title)
            Result.success(comp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboard(): Result<DashboardResponse> {
        return try {
            val dashboard = apiService.getDashboard()
            Result.success(dashboard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(): Result<List<NotificationItem>> {
        return try {
            val notifs = apiService.getNotifications()
            Result.success(notifs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Offers & Free Games
    suspend fun getFreeGames(): Result<List<FreeGameOffer>> {
        return try {
            val offers = apiService.getFreeGames()
            Result.success(offers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Library & Statistics
    suspend fun getUnifiedLibrary(): Result<UnifiedLibraryResponse> {
        return try {
            val steamUser = preferences.steamUserFlow.first()
            val epicUser = preferences.epicUserFlow.first()
            val response = apiService.getUnifiedLibrary(steamUser, epicUser)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Wishlist (Room DB Local Persistence)
    fun getWishlist(): Flow<List<WishlistEntity>> = wishlistDao.getAllWishlist()

    fun isWishlisted(gameId: String): Flow<Boolean> = wishlistDao.isWishlisted(gameId)

    suspend fun addToWishlist(game: GameSummary) {
        val entity = WishlistEntity(
            gameId = game.gameId,
            title = game.title,
            coverImage = game.coverImage,
            lowestPrice = game.lowestPrice ?: 0.0,
            originalPrice = game.originalPrice ?: 0.0,
            discountPercent = game.highestDiscountPercent,
            cheapestStore = game.cheapestStore?.name ?: "Unknown",
            formattedLowestPrice = game.formattedLowestPrice ?: "₹0"
        )
        wishlistDao.insert(entity)
    }

    suspend fun removeFromWishlist(gameId: String) {
        wishlistDao.deleteByGameId(gameId)
    }

    // Recent Searches
    fun getRecentSearches(): Flow<List<RecentSearchEntity>> = recentSearchDao.getRecentSearches()

    suspend fun deleteRecentSearch(query: String) = recentSearchDao.delete(query)

    suspend fun clearRecentSearches() = recentSearchDao.clearAll()

    // Accounts
    suspend fun connectSteam(identifier: String, displayName: String?): Result<ConnectAccountResponse> {
        return try {
            val resp = apiService.connectSteam(ConnectAccountRequest(identifier, displayName))
            preferences.setSteamUser(identifier)
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connectEpic(identifier: String, displayName: String?): Result<ConnectAccountResponse> {
        return try {
            val resp = apiService.connectEpic(ConnectAccountRequest(identifier, displayName))
            preferences.setEpicUser(identifier)
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun disconnectStore(store: String): Result<Unit> {
        return try {
            apiService.disconnectAccount(store.lowercase())
            if (store.equals("steam", ignoreCase = true)) {
                preferences.setSteamUser(null)
            } else if (store.equals("epic", ignoreCase = true)) {
                preferences.setEpicUser(null)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
