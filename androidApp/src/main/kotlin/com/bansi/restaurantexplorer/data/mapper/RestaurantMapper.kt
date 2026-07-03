package com.bansi.restaurantexplorer.data.mapper

import com.bansi.restaurantexplorer.data.local.entity.RestaurantEntity
import com.bansi.restaurantexplorer.data.remote.dto.RestaurantDto
import com.bansi.restaurantexplorer.domain.model.CuisineType
import com.bansi.restaurantexplorer.domain.model.Restaurant

fun RestaurantDto.toEntity(isBookmarked: Boolean = false, lastUpdated: Long): RestaurantEntity {
    return RestaurantEntity(
        id = id,
        name = name,
        cuisine = cuisine,
        rating = rating,
        priceLevel = priceLevel,
        address = address,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        description = description,
        isBookmarked = isBookmarked,
        lastUpdated = lastUpdated,
    )
}

fun RestaurantEntity.toDomain(distanceKm: Double? = null, recommendationScore: Double = 0.0): Restaurant {
    return Restaurant(
        id = id,
        name = name,
        cuisine = CuisineType.fromRaw(cuisine),
        rating = rating,
        priceLevel = priceLevel,
        address = address,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        description = description,
        isBookmarked = isBookmarked,
        distanceKm = distanceKm,
        recommendationScore = recommendationScore,
    )
}

fun Restaurant.toEntity(lastUpdated: Long): RestaurantEntity {
    return RestaurantEntity(
        id = id,
        name = name,
        cuisine = cuisine.name,
        rating = rating,
        priceLevel = priceLevel,
        address = address,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        description = description,
        isBookmarked = isBookmarked,
        lastUpdated = lastUpdated,
    )
}
