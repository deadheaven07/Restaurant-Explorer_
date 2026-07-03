package com.bansi.restaurantexplorer.data.remote.dto

data class RestaurantDto(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Float,
    val priceLevel: Int,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val description: String,
)

data class RestaurantsResponse(
    val restaurants: List<RestaurantDto>,
)
