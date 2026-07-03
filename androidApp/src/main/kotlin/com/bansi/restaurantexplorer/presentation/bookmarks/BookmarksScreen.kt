package com.bansi.restaurantexplorer.presentation.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansi.restaurantexplorer.presentation.components.EmptyState
import com.bansi.restaurantexplorer.presentation.components.RestaurantCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onRestaurantClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Saved Restaurants") })
        },
    ) { padding ->
        when (val state = uiState) {
            is BookmarksUiState.Loading -> {
                // Could show a progress indicator here if desired
            }
            is BookmarksUiState.Success -> {
                if (state.bookmarkedRestaurants.isEmpty()) {
                    EmptyState(
                        title = "No bookmarks yet",
                        message = "Your favorite restaurants will appear here.",
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.bookmarkedRestaurants, key = { it.id }) { restaurant ->
                            RestaurantCard(
                                restaurant = restaurant,
                                onClick = { onRestaurantClick(restaurant.id) },
                                onBookmarkClick = { viewModel.toggleBookmark(restaurant.id) },
                            )
                        }
                    }
                }
            }
            is BookmarksUiState.Error -> {
                EmptyState(
                    title = "Error",
                    message = state.message,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}
