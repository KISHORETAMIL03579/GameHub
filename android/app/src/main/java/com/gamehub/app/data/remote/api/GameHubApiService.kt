package com.gamehub.app.data.remote.api

import com.gamehub.app.data.remote.model.*
import retrofit2.http.*

interface GameHubApiService {

    @GET("api/search")
    suspend fun searchGames(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("store") store: String? = null
    ): List<GameSummary>

    @GET("api/dashboard")
    suspend fun getDashboard(): DashboardResponse

    @GET("api/notifications")
    suspend fun getNotifications(): List<NotificationItem>

    @GET("api/game/{game_id}")
    suspend fun getGameDetails(
        @Path("game_id") gameId: String,
        @Query("title") title: String? = null
    ): GameDetails

    @GET("api/prices/{game_id}")
    suspend fun comparePrices(
        @Path("game_id") gameId: String,
        @Query("title") title: String? = null
    ): PriceComparisonResponse

    @GET("api/free-games")
    suspend fun getFreeGames(): List<FreeGameOffer>

    @GET("api/offers")
    suspend fun getOffers(): List<FreeGameOffer>

    @GET("api/library/unified")
    suspend fun getUnifiedLibrary(
        @Query("steam_id") steamId: String? = null,
        @Query("epic_id") epicId: String? = null
    ): UnifiedLibraryResponse

    @POST("api/accounts/steam/connect")
    suspend fun connectSteam(
        @Body request: ConnectAccountRequest
    ): ConnectAccountResponse

    @POST("api/accounts/epic/connect")
    suspend fun connectEpic(
        @Body request: ConnectAccountRequest
    ): ConnectAccountResponse

    @DELETE("api/accounts/{store}")
    suspend fun disconnectAccount(
        @Path("store") store: String
    ): Map<String, String>
}
