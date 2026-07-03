package com.bansi.restaurantexplorer.domain.usecase

import com.bansi.restaurantexplorer.domain.model.FilterOptions
import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.model.UserLocation
import com.bansi.restaurantexplorer.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    operator fun invoke(
        filters: FilterOptions,
        location: UserLocation?,
    ): Flow<List<Restaurant>> {
        return repository.observeRestaurants(filters, location)
    }
}
