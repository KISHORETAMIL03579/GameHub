package com.gamehub.app.data.local.dao

import androidx.room.*
import com.gamehub.app.data.local.entity.RecentSearchEntity
import com.gamehub.app.data.local.entity.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist ORDER BY addedTimestamp DESC")
    fun getAllWishlist(): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlist WHERE gameId = :gameId LIMIT 1")
    suspend fun getWishlistItem(gameId: String): WishlistEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE gameId = :gameId)")
    fun isWishlisted(gameId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WishlistEntity)

    @Delete
    suspend fun delete(item: WishlistEntity)

    @Query("DELETE FROM wishlist WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: String)
}

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE `query` = :query")
    suspend fun delete(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAll()
}
