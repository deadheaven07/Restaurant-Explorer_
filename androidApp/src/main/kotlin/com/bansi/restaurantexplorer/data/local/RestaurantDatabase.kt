package com.bansi.restaurantexplorer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bansi.restaurantexplorer.data.local.dao.RestaurantDao
import com.bansi.restaurantexplorer.data.local.dao.SearchHistoryDao
import com.bansi.restaurantexplorer.data.local.dao.ViewHistoryDao
import com.bansi.restaurantexplorer.data.local.entity.RestaurantEntity
import com.bansi.restaurantexplorer.data.local.entity.SearchHistoryEntity
import com.bansi.restaurantexplorer.data.local.entity.ViewHistoryEntity

@Database(
    entities = [
        RestaurantEntity::class,
        SearchHistoryEntity::class,
        ViewHistoryEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class RestaurantDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun viewHistoryDao(): ViewHistoryDao
}
