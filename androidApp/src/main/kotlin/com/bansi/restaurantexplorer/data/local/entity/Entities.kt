package com.bansi.restaurantexplorer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cuisine: String,
    val rating: Float,
    val priceLevel: Int,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val description: String,
    val isBookmarked: Boolean,
    val lastUpdated: Long,
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long,
)

@Entity(tableName = "view_history")
data class ViewHistoryEntity(
    @PrimaryKey val restaurantId: String,
    val viewCount: Int,
    val lastViewed: Long,
)
