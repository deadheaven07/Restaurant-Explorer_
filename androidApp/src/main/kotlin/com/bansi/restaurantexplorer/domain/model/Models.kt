package com.bansi.restaurantexplorer.domain.model

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: CuisineType,
    val rating: Float,
    val priceLevel: Int,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val description: String,
    val isBookmarked: Boolean,
    val distanceKm: Double? = null,
    val recommendationScore: Double = 0.0,
)

enum class CuisineType(val displayName: String) {
    ITALIAN("Italian"),
    JAPANESE("Japanese"),
    MEXICAN("Mexican"),
    INDIAN("Indian"),
    AMERICAN("American"),
    CHINESE("Chinese"),
    THAI("Thai"),
    FRENCH("French"),
    MEDITERRANEAN("Mediterranean"),
    KOREAN("Korean"),
    ;

    companion object {
        fun fromRaw(value: String): CuisineType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: AMERICAN
    }
}

data class FilterOptions(
    val cuisines: Set<CuisineType> = emptySet(),
    val minRating: Float = 0f,
    val maxPriceLevel: Int = 4,
    val maxDistanceKm: Double? = null,
    val sortBy: SortOption = SortOption.RECOMMENDED,
)

enum class SortOption(val displayName: String) {
    RECOMMENDED("Recommended"),
    DISTANCE("Distance"),
    RATING("Rating"),
    PRICE_LOW("Price: Low to High"),
    PRICE_HIGH("Price: High to Low"),
}

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
)

data class UserPreferences(
    val preferredCuisines: Set<CuisineType> = emptySet(),
    val recentSearches: List<String> = emptyList(),
    val viewedRestaurantIds: Map<String, Int> = emptyMap(),
)
