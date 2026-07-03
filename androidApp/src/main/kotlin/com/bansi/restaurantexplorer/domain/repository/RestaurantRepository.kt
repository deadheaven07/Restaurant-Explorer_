package com.bansi.restaurantexplorer.domain.repository

import com.bansi.restaurantexplorer.domain.model.FilterOptions
import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.model.UserLocation
import com.bansi.restaurantexplorer.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {
    fun observeRestaurants(
        filters: FilterOptions,
        location: UserLocation?,
    ): Flow<List<Restaurant>>

    fun observeBookmarkedRestaurants(location: UserLocation?): Flow<List<Restaurant>>

    fun observeSearchResults(
        query: String,
        filters: FilterOptions,
        location: UserLocation?,
    ): Flow<List<Restaurant>>

    fun observeRecommendedRestaurants(location: UserLocation?): Flow<List<Restaurant>>

    suspend fun getRestaurantById(id: String): Restaurant?

    suspend fun toggleBookmark(restaurantId: String)

    suspend fun recordSearch(query: String)

    suspend fun recordView(restaurantId: String)

    suspend fun refreshRestaurants()

    suspend fun getUserPreferences(): UserPreferences

    fun observeRecentSearches(): Flow<List<String>>
}
