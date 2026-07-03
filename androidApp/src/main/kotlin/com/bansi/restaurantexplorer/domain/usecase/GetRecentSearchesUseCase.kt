package com.bansi.restaurantexplorer.domain.usecase

import com.bansi.restaurantexplorer.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentSearchesUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    operator fun invoke(): Flow<List<String>> {
        return repository.observeRecentSearches()
    }
}
