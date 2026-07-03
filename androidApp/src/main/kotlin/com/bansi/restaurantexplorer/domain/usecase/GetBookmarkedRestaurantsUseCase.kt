package com.bansi.restaurantexplorer.domain.usecase

import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.model.UserLocation
import com.bansi.restaurantexplorer.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarkedRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    operator fun invoke(location: UserLocation?): Flow<List<Restaurant>> {
        return repository.observeBookmarkedRestaurants(location)
    }
}
