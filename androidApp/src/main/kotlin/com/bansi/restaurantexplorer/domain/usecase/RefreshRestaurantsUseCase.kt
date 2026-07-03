package com.bansi.restaurantexplorer.domain.usecase

import com.bansi.restaurantexplorer.domain.repository.RestaurantRepository
import javax.inject.Inject

class RefreshRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    suspend operator fun invoke() {
        repository.refreshRestaurants()
    }
}
