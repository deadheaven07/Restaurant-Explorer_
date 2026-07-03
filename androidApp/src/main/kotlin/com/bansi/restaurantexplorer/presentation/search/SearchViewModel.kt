package com.bansi.restaurantexplorer.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansi.restaurantexplorer.domain.model.FilterOptions
import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.model.UserLocation
import com.bansi.restaurantexplorer.domain.usecase.GetRecentSearchesUseCase
import com.bansi.restaurantexplorer.domain.usecase.RecordSearchUseCase
import com.bansi.restaurantexplorer.domain.usecase.SearchRestaurantsUseCase
import com.bansi.restaurantexplorer.domain.usecase.ToggleBookmarkUseCase
import com.bansi.restaurantexplorer.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<Restaurant> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val filterOptions: FilterOptions = FilterOptions(),
    val isFilterSheetVisible: Boolean = false,
    val isSearching: Boolean = false,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRestaurantsUseCase: SearchRestaurantsUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val recordSearchUseCase: RecordSearchUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filterOptions = MutableStateFlow(FilterOptions())
    private val userLocation = MutableStateFlow<UserLocation?>(locationProvider.defaultLocation)
    private val isFilterSheetVisible = MutableStateFlow(false)

    val uiState: StateFlow<SearchUiState> = combine(
        searchQuery.debounce(300).distinctUntilChanged(),
        filterOptions,
        userLocation,
        isFilterSheetVisible,
    ) { currentQuery, currentFilters, currentLocation, showFilterSheet ->
        SearchParams(currentQuery, currentFilters, currentLocation, showFilterSheet)
    }.flatMapLatest { params ->
        combine(
            searchRestaurantsUseCase(params.query, params.filters, params.location),
            getRecentSearchesUseCase(),
        ) { results, recentSearches ->
            SearchUiState(
                searchQuery = params.query,
                searchResults = results,
                recentSearches = recentSearches,
                filterOptions = params.filters,
                isFilterSheetVisible = params.showFilters,
                isSearching = params.query.isNotBlank(),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState(),
    )

    init {
        loadLocation()
    }

    private fun loadLocation() {
        viewModelScope.launch {
            val current = locationProvider.getCurrentLocation()
            userLocation.value = current ?: locationProvider.defaultLocation
        }
    }

    fun onQueryChange(value: String) {
        searchQuery.value = value
    }

    fun submitSearch() {
        viewModelScope.launch {
            recordSearchUseCase(searchQuery.value)
        }
    }

    fun selectRecentSearch(value: String) {
        searchQuery.value = value
        submitSearch()
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

    private data class SearchParams(
        val query: String,
        val filters: FilterOptions,
        val location: UserLocation?,
        val showFilters: Boolean,
    )
}
