package com.bansi.restaurantexplorer.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.usecase.GetBookmarkedRestaurantsUseCase
import com.bansi.restaurantexplorer.domain.usecase.ToggleBookmarkUseCase
import com.bansi.restaurantexplorer.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BookmarksUiState {
    data object Loading : BookmarksUiState()
    data class Success(val bookmarkedRestaurants: List<Restaurant>) : BookmarksUiState()
    data class Error(val message: String) : BookmarksUiState()
}

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val getBookmarkedRestaurantsUseCase: GetBookmarkedRestaurantsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val userLocation = MutableStateFlow(locationProvider.defaultLocation)

    val uiState: StateFlow<BookmarksUiState> = userLocation
        .flatMapLatest { location ->
            getBookmarkedRestaurantsUseCase(location)
        }
        .map { bookmarks ->
            BookmarksUiState.Success(bookmarks)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BookmarksUiState.Loading,
        )

    init {
        loadLocation()
    }

    private fun loadLocation() {
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                userLocation.value = location
            }
        }
    }

    fun toggleBookmark(restaurantId: String) {
        viewModelScope.launch {
            toggleBookmarkUseCase(restaurantId)
        }
    }
}
