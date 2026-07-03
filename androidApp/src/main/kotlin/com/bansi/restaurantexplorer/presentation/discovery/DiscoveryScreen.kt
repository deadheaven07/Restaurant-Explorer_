package com.bansi.restaurantexplorer.presentation.discovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansi.restaurantexplorer.presentation.components.EmptyState
import com.bansi.restaurantexplorer.presentation.components.FilterBottomSheet
import com.bansi.restaurantexplorer.presentation.components.RestaurantCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    onRestaurantClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiscoveryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isFilterSheetVisible) {
        FilterBottomSheet(
            filters = uiState.filterOptions,
            onFiltersChange = viewModel::updateFilters,
            onDismiss = { viewModel.toggleFilterSheet(false) },
            onApply = { viewModel.toggleFilterSheet(false) },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Discover") },
                actions = {
                    if (uiState.isUsingDefaultLocation) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Using default location",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = { viewModel.toggleFilterSheet(true) }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.recommendations.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recommended for you",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.recommendations, key = { it.id }) { restaurant ->
                                RestaurantCard(
                                    restaurant = restaurant,
                                    onClick = { onRestaurantClick(restaurant.id) },
                                    onBookmarkClick = { viewModel.toggleBookmark(restaurant.id) },
                                    modifier = Modifier.fillParentMaxWidth(0.85f),
                                    showRecommendationBadge = true,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Text(
                        text = "All restaurants",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.restaurants.isEmpty() && !uiState.isLoading) {
                    item {
                        EmptyState(
                            title = "No restaurants found",
                            message = "Try adjusting your filters or pull to refresh.",
                        )
                    }
                } else {
                    items(uiState.restaurants, key = { it.id }) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick = { onRestaurantClick(restaurant.id) },
                            onBookmarkClick = { viewModel.toggleBookmark(restaurant.id) },
                        )
                    }
                }
            }
        }
    }
}
