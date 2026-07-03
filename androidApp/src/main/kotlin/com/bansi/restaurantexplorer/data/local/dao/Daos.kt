package com.bansi.restaurantexplorer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bansi.restaurantexplorer.data.local.entity.RestaurantEntity
import com.bansi.restaurantexplorer.data.local.entity.SearchHistoryEntity
import com.bansi.restaurantexplorer.data.local.entity.ViewHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants ORDER BY name ASC")
    fun observeAll(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE isBookmarked = 1 ORDER BY name ASC")
    fun observeBookmarked(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(restaurants: List<RestaurantEntity>)

    @Query("UPDATE restaurants SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Query("SELECT COUNT(*) FROM restaurants")
    suspend fun count(): Int
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun observeRecent(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteQuery(query: String)
}

@Dao
interface ViewHistoryDao {
    @Query("SELECT * FROM view_history")
    fun observeAll(): Flow<List<ViewHistoryEntity>>

    @Transaction
    suspend fun recordView(restaurantId: String, timestamp: Long) {
        val existing = getById(restaurantId)
        if (existing == null) {
            insert(
                ViewHistoryEntity(
                    restaurantId = restaurantId,
                    viewCount = 1,
                    lastViewed = timestamp,
                ),
            )
        } else {
            update(
                existing.copy(
                    viewCount = existing.viewCount + 1,
                    lastViewed = timestamp,
                ),
            )
        }
    }

    @Query("SELECT * FROM view_history WHERE restaurantId = :restaurantId LIMIT 1")
    suspend fun getById(restaurantId: String): ViewHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ViewHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entry: ViewHistoryEntity)
}
