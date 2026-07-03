package com.bansi.restaurantexplorer.domain.usecase

import com.bansi.restaurantexplorer.domain.model.CuisineType
import com.bansi.restaurantexplorer.domain.model.FilterOptions
import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.model.SortOption
import com.bansi.restaurantexplorer.domain.model.UserLocation
import com.bansi.restaurantexplorer.domain.model.UserPreferences
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Core engine for calculating restaurant personalization scores and processing lists.
 * Scores are influenced by user history (views, bookmarks, searches) and physical proximity.
 */
object RecommendationEngine {

    fun enrich(
        restaurants: List<Restaurant>,
        location: UserLocation?,
        preferences: UserPreferences,
    ): List<Restaurant> {
        return restaurants.map { restaurant ->
            val distanceKm = location?.let { calculateDistanceKm(it, restaurant.latitude, restaurant.longitude) }
            val score = calculateRecommendationScore(restaurant, distanceKm, preferences)
            restaurant.copy(distanceKm = distanceKm, recommendationScore = score)
        }
    }

    fun applyFilters(restaurants: List<Restaurant>, filters: FilterOptions): List<Restaurant> {
        return restaurants.filter { restaurant ->
            (filters.cuisines.isEmpty() || restaurant.cuisine in filters.cuisines) &&
                restaurant.rating >= filters.minRating &&
                restaurant.priceLevel <= filters.maxPriceLevel &&
                (filters.maxDistanceKm == null || (restaurant.distanceKm ?: Double.MAX_VALUE) <= filters.maxDistanceKm)
        }
    }

    fun sort(restaurants: List<Restaurant>, sortBy: SortOption): List<Restaurant> {
        return when (sortBy) {
            SortOption.RECOMMENDED -> restaurants.sortedByDescending { it.recommendationScore }
            SortOption.DISTANCE -> restaurants.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
            SortOption.RATING -> restaurants.sortedByDescending { it.rating }
            SortOption.PRICE_LOW -> restaurants.sortedBy { it.priceLevel }
            SortOption.PRICE_HIGH -> restaurants.sortedByDescending { it.priceLevel }
        }
    }

    fun search(restaurants: List<Restaurant>, query: String): List<Restaurant> {
        if (query.isBlank()) return restaurants
        val normalized = query.trim().lowercase()
        return restaurants.filter { restaurant ->
            restaurant.name.lowercase().contains(normalized) ||
                restaurant.cuisine.displayName.lowercase().contains(normalized) ||
                restaurant.address.lowercase().contains(normalized) ||
                restaurant.description.lowercase().contains(normalized)
        }
    }

    private fun calculateRecommendationScore(
        restaurant: Restaurant,
        distanceKm: Double?,
        preferences: UserPreferences,
    ): Double {
        var score = restaurant.rating * 10.0

        if (restaurant.cuisine in preferences.preferredCuisines) {
            score += 25.0
        }

        val viewCount = preferences.viewedRestaurantIds[restaurant.id] ?: 0
        score += viewCount * 5.0

        preferences.recentSearches.forEach { search ->
            val normalized = search.lowercase()
            if (
                restaurant.name.lowercase().contains(normalized) ||
                restaurant.cuisine.displayName.lowercase().contains(normalized)
            ) {
                score += 8.0
            }
        }

        if (restaurant.isBookmarked) {
            score += 15.0
        }

        // Proximity is a factor, but weighted moderately so that quality/preference
        // remains the primary driver of recommendations.
        distanceKm?.let { distance ->
            score -= distance * 2.5
        }

        return score
    }

    fun calculateDistanceKm(from: UserLocation, latitude: Double, longitude: Double): Double {
        val earthRadiusKm = 6371.0
        val latDistance = Math.toRadians(latitude - from.latitude)
        val lonDistance = Math.toRadians(longitude - from.longitude)
        val a = sin(latDistance / 2).pow(2.0) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(latitude)) *
            sin(lonDistance / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun derivePreferredCuisines(
        restaurants: List<Restaurant>,
        viewedIds: Map<String, Int>,
        bookmarkedOnly: List<CuisineType>,
    ): Set<CuisineType> {
        val fromViews = viewedIds.entries
            .sortedByDescending { it.value }
            .mapNotNull { entry -> restaurants.find { it.id == entry.key }?.cuisine }
            .take(3)
        return (bookmarkedOnly + fromViews).toSet()
    }
}
