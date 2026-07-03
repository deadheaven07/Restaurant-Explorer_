package com.bansi.restaurantexplorer.presentation.search

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansi.restaurantexplorer.presentation.components.EmptyState
import com.bansi.restaurantexplorer.presentation.components.FilterBottomSheet
import com.bansi.restaurantexplorer.presentation.components.RestaurantCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onRestaurantClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
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
                title = { Text("Search") },
                actions = {
                    IconButton(onClick = { viewModel.toggleFilterSheet(true) }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name, cuisine, or area") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                )
            }

            if (uiState.recentSearches.isNotEmpty() && uiState.searchQuery.isBlank()) {
                item {
                    Text(text = "Recent searches")
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.recentSearches.forEach { search ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.selectRecentSearch(search) },
                                label = { Text(search) },
                            )
                        }
                    }
                }
            }

            if (uiState.isSearching && uiState.searchResults.isEmpty()) {
                item {
                    EmptyState(
                        title = "No matches",
                        message = "Try a different keyword or adjust filters.",
                    )
                }
            } else {
                items(uiState.searchResults, key = { it.id }) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        onClick = {
                            viewModel.submitSearch()
                            onRestaurantClick(restaurant.id)
                        },
                        onBookmarkClick = { viewModel.toggleBookmark(restaurant.id) },
                    )
                }
            }
        }
    }
}
