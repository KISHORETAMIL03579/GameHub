package com.gamehub.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gamehub.app.data.local.dao.RecentSearchDao
import com.gamehub.app.data.local.dao.WishlistDao
import com.gamehub.app.data.local.entity.RecentSearchEntity
import com.gamehub.app.data.local.entity.WishlistEntity

@Database(
    entities = [WishlistEntity::class, RecentSearchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameHubDatabase : RoomDatabase() {
    abstract fun wishlistDao(): WishlistDao
    abstract fun recentSearchDao(): RecentSearchDao

    companion object {
        @Volatile
        private var INSTANCE: GameHubDatabase? = null

        fun getInstance(context: Context): GameHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameHubDatabase::class.java,
                    "gamehub_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
