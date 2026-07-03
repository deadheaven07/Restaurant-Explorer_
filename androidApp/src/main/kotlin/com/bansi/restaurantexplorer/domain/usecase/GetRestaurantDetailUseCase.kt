package com.bansi.restaurantexplorer.domain.usecase

import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.model.UserLocation
import com.bansi.restaurantexplorer.domain.repository.RestaurantRepository
import javax.inject.Inject

class GetRestaurantDetailUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    suspend operator fun invoke(id: String, location: UserLocation?): Restaurant? {
        val restaurant = repository.getRestaurantById(id) ?: return null
        val preferences = repository.getUserPreferences()
        
        // Enrich with distance and recommendation score for the detail view
        return RecommendationEngine.enrich(listOf(restaurant), location, preferences).firstOrNull()
    }
}
