package com.bansi.restaurantexplorer.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.usecase.GetRestaurantDetailUseCase
import com.bansi.restaurantexplorer.domain.usecase.RecordViewUseCase
import com.bansi.restaurantexplorer.domain.usecase.ToggleBookmarkUseCase
import com.bansi.restaurantexplorer.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Success(val restaurant: Restaurant) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val recordViewUseCase: RecordViewUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val restaurantId: String = checkNotNull(savedStateHandle["restaurantId"])
    private val userLocation = MutableStateFlow(locationProvider.defaultLocation)

    val uiState: StateFlow<DetailUiState> = userLocation
        .map { location ->
            val restaurant = getRestaurantDetailUseCase(restaurantId, location)
            if (restaurant != null) {
                DetailUiState.Success(restaurant)
            } else {
                DetailUiState.Error("Restaurant not found")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailUiState.Loading,
        )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            recordViewUseCase(restaurantId)
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                userLocation.value = location
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            toggleBookmarkUseCase(restaurantId)
        }
    }
}
