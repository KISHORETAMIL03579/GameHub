package com.gamehub.app.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StoreType {
    @SerialName("Steam")
    STEAM,
    @SerialName("Epic")
    EPIC,
    @SerialName("Unknown")
    UNKNOWN
}

@Serializable
data class StorePriceInfo(
    val store: StoreType,
    @SerialName("store_item_id") val storeItemId: String,
    @SerialName("store_url") val storeUrl: String,
    val price: Double,
    @SerialName("original_price") val originalPrice: Double,
    @SerialName("discount_percent") val discountPercent: Int = 0,
    val currency: String = "INR",
    @SerialName("is_free") val isFree: Boolean = false,
    @SerialName("in_stock") val inStock: Boolean = true,
    @SerialName("formatted_price") val formattedPrice: String = "₹0",
    @SerialName("formatted_original_price") val formattedOriginalPrice: String = "₹0"
)

@Serializable
data class GameSummary(
    @SerialName("game_id") val gameId: String,
    val title: String,
    @SerialName("cover_image") val coverImage: String? = null,
    @SerialName("cheapest_store") val cheapestStore: StoreType? = null,
    @SerialName("lowest_price") val lowestPrice: Double? = null,
    @SerialName("original_price") val originalPrice: Double? = null,
    @SerialName("highest_discount_percent") val highestDiscountPercent: Int = 0,
    @SerialName("formatted_lowest_price") val formattedLowestPrice: String? = null,
    val stores: List<StorePriceInfo> = emptyList()
)

@Serializable
data class GameDetails(
    @SerialName("game_id") val gameId: String,
    val title: String,
    @SerialName("normalized_title") val normalizedTitle: String,
    val description: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    @SerialName("cover_image") val coverImage: String? = null,
    @SerialName("banner_image") val bannerImage: String? = null,
    val screenshots: List<String> = emptyList(),
    val developer: String? = null,
    val publisher: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val genres: List<String> = emptyList(),
    val currency: String = "INR",
    @SerialName("cheapest_store") val cheapestStore: StoreType? = null,
    @SerialName("potential_savings") val potentialSavings: Double = 0.0,
    val stores: List<StorePriceInfo> = emptyList()
)

@Serializable
data class PriceComparisonResponse(
    @SerialName("game_id") val gameId: String,
    val title: String,
    val currency: String = "INR",
    @SerialName("cheapest_store") val cheapestStore: StoreType? = null,
    @SerialName("potential_savings") val potentialSavings: Double = 0.0,
    @SerialName("potential_savings_formatted") val potentialSavingsFormatted: String = "₹0",
    val stores: List<StorePriceInfo> = emptyList(),
    val recommendation: String? = null
)

@Serializable
data class FreeGameOffer(
    val id: String,
    val title: String,
    val store: StoreType,
    @SerialName("cover_image") val coverImage: String? = null,
    @SerialName("store_url") val storeUrl: String,
    @SerialName("original_price") val originalPrice: Double = 0.0,
    @SerialName("formatted_original_price") val formattedOriginalPrice: String = "₹0",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class LibraryGame(
    val id: String,
    val title: String,
    val store: StoreType,
    @SerialName("store_item_id") val storeItemId: String,
    @SerialName("cover_image") val coverImage: String? = null,
    @SerialName("playtime_minutes") val playtimeMinutes: Int = 0,
    @SerialName("playtime_hours") val playtimeHours: Double = 0.0,
    @SerialName("last_played") val lastPlayed: String? = null,
    @SerialName("estimated_value_inr") val estimatedValueInr: Double = 0.0
)

@Serializable
data class UnifiedLibraryResponse(
    @SerialName("total_games") val totalGames: Int = 0,
    @SerialName("steam_count") val steamCount: Int = 0,
    @SerialName("epic_count") val epicCount: Int = 0,
    @SerialName("duplicate_count") val duplicateCount: Int = 0,
    @SerialName("estimated_total_value_inr") val estimatedTotalValueInr: Double = 0.0,
    @SerialName("total_playtime_hours") val totalPlaytimeHours: Double = 0.0,
    val duplicates: List<String> = emptyList(),
    val games: List<LibraryGame> = emptyList()
)

@Serializable
data class ConnectAccountRequest(
    @SerialName("account_identifier") val accountIdentifier: String,
    @SerialName("display_name") val displayName: String? = null
)

@Serializable
data class ConnectAccountResponse(
    val success: Boolean,
    val store: StoreType,
    @SerialName("account_identifier") val accountIdentifier: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("games_imported") val gamesImported: Int = 0,
    val message: String
)

@Serializable
data class FeaturedGame(
    @SerialName("game_id") val gameId: String,
    val title: String,
    val subtitle: String,
    @SerialName("banner_image") val bannerImage: String,
    @SerialName("lowest_price") val lowestPrice: Double,
    @SerialName("formatted_lowest_price") val formattedLowestPrice: String,
    @SerialName("cheapest_store") val cheapestStore: StoreType,
    @SerialName("discount_percent") val discountPercent: Int = 0,
    val stores: List<StorePriceInfo> = emptyList()
)

@Serializable
data class DashboardDeal(
    @SerialName("game_id") val gameId: String,
    val title: String,
    @SerialName("cover_image") val coverImage: String,
    val store: StoreType,
    @SerialName("sale_price") val salePrice: Double,
    @SerialName("original_price") val originalPrice: Double,
    @SerialName("formatted_sale_price") val formattedSalePrice: String,
    @SerialName("discount_percent") val discountPercent: Int = 0
)

@Serializable
data class RecentGame(
    @SerialName("game_id") val gameId: String,
    val title: String,
    @SerialName("cover_image") val coverImage: String
)

@Serializable
data class DashboardResponse(
    @SerialName("steam_count") val steamCount: Int = 124,
    @SerialName("epic_count") val epicCount: Int = 56,
    @SerialName("featured_game") val featuredGame: FeaturedGame,
    @SerialName("best_deals") val bestDeals: List<DashboardDeal> = emptyList(),
    @SerialName("recently_added") val recentlyAdded: List<RecentGame> = emptyList()
)

@Serializable
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    @SerialName("game_id") val gameId: String? = null,
    val store: StoreType? = null,
    val timestamp: String = "Just now",
    @SerialName("is_read") var isRead: Boolean = false,
    @SerialName("sale_price") val salePrice: String? = null,
    @SerialName("original_price") val originalPrice: String? = null,
    @SerialName("discount_percent") val discountPercent: Int = 0
)
