package com.bansi.restaurantexplorer.domain.usecase

import com.bansi.restaurantexplorer.domain.model.FilterOptions
import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.model.UserLocation
import com.bansi.restaurantexplorer.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    operator fun invoke(
        query: String,
        filters: FilterOptions,
        location: UserLocation?,
    ): Flow<List<Restaurant>> {
        return repository.observeSearchResults(query, filters, location)
    }
}
