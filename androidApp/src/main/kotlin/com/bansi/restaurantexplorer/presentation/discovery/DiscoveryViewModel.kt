package com.bansi.restaurantexplorer.presentation.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansi.restaurantexplorer.domain.model.FilterOptions
import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.model.UserLocation
import com.bansi.restaurantexplorer.domain.usecase.GetRecommendedRestaurantsUseCase
import com.bansi.restaurantexplorer.domain.usecase.GetRestaurantsUseCase
import com.bansi.restaurantexplorer.domain.usecase.RefreshRestaurantsUseCase
import com.bansi.restaurantexplorer.domain.usecase.ToggleBookmarkUseCase
import com.bansi.restaurantexplorer.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoveryUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val recommendations: List<Restaurant> = emptyList(),
    val filterOptions: FilterOptions = FilterOptions(),
    val userLocation: UserLocation? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isFilterSheetVisible: Boolean = false,
    val error: String? = null,
    val isUsingDefaultLocation: Boolean = false,
)

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    private val getRecommendedRestaurantsUseCase: GetRecommendedRestaurantsUseCase,
    private val refreshRestaurantsUseCase: RefreshRestaurantsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val filterOptions = MutableStateFlow(FilterOptions())
    private val userLocation = MutableStateFlow<UserLocation?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val isFilterSheetVisible = MutableStateFlow(false)
    private val isUsingDefaultLocation = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    // Combine multiple UI trigger flows into a single params object.
    // We split them into two triples because the 'combine' operator only supports up to 5 flows directly.
    val uiState: StateFlow<DiscoveryUiState> = combine(
        combine(filterOptions, userLocation, isRefreshing) { filters, location, refreshing ->
            Triple(filters, location, refreshing)
        },
        combine(isFilterSheetVisible, isUsingDefaultLocation, error) { showFilters, defaultLoc, err ->
            Triple(showFilters, defaultLoc, err)
        },
    ) { part1, part2 ->
        DiscoveryParams(
            filters = part1.first,
            location = part1.second,
            isRefreshing = part1.third,
            isFilterSheetVisible = part2.first,
            isUsingDefaultLocation = part2.second,
            error = part2.third,
        )
    }.flatMapLatest { params ->
        combine(
            getRestaurantsUseCase(params.filters, params.location),
            getRecommendedRestaurantsUseCase(params.location),
        ) { restaurants, recommendations ->
            DiscoveryUiState(
                restaurants = restaurants,
                recommendations = recommendations,
                filterOptions = params.filters,
                userLocation = params.location,
                isLoading = false,
                isRefreshing = params.isRefreshing,
                isFilterSheetVisible = params.isFilterSheetVisible,
                isUsingDefaultLocation = params.isUsingDefaultLocation,
                error = params.error,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DiscoveryUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                refreshRestaurantsUseCase()
                loadCurrentLocation()
            } catch (e: Exception) {
                error.value = "Failed to update restaurant list. Using offline data."
            } finally {
                isRefreshing.value = false
            }
        }
    }

    private fun loadCurrentLocation() {
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                userLocation.value = location
                isUsingDefaultLocation.value = false
            } else {
                userLocation.value = locationProvider.defaultLocation
                isUsingDefaultLocation.value = true
            }
        }
    }

    fun updateFilters(newFilters: FilterOptions) {
        filterOptions.value = newFilters
    }

    fun toggleFilterSheet(isVisible: Boolean) {
        isFilterSheetVisible.value = isVisible
    }

    fun toggleBookmark(restaurantId: String) {
        viewModelScope.launch {
            toggleBookmarkUseCase(restaurantId)
        }
    }

    private data class DiscoveryParams(
        val filters: FilterOptions,
        val location: UserLocation?,
        val isRefreshing: Boolean,
        val isFilterSheetVisible: Boolean,
        val isUsingDefaultLocation: Boolean,
        val error: String?,
    )
}
