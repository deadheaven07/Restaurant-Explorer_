package com.bansi.restaurantexplorer.domain.usecase

import com.bansi.restaurantexplorer.domain.repository.RestaurantRepository
import javax.inject.Inject

class RecordSearchUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    suspend operator fun invoke(query: String) {
        repository.recordSearch(query)
    }
}
