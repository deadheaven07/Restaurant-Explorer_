package com.bansi.restaurantexplorer.data.remote.api

import com.bansi.restaurantexplorer.data.remote.dto.RestaurantsResponse
import retrofit2.http.GET

interface RestaurantApi {
    @GET("restaurants")
    suspend fun getRestaurants(): RestaurantsResponse
}
