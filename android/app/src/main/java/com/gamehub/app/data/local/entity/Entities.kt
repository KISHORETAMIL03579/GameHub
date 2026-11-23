package com.gamehub.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class WishlistEntity(
    @PrimaryKey
    val gameId: String,
    val title: String,
    val coverImage: String?,
    val lowestPrice: Double,
    val originalPrice: Double,
    val discountPercent: Int,
    val cheapestStore: String,
    val formattedLowestPrice: String,
    val targetPriceAlert: Double? = null,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
