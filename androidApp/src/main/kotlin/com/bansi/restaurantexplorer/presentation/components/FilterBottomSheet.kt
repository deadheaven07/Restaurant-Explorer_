package com.bansi.restaurantexplorer.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bansi.restaurantexplorer.domain.model.CuisineType
import com.bansi.restaurantexplorer.domain.model.FilterOptions
import com.bansi.restaurantexplorer.domain.model.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    filters: FilterOptions,
    onFiltersChange: (FilterOptions) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(text = "Filters", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Cuisine", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CuisineType.entries.forEach { cuisine ->
                    FilterChip(
                        selected = cuisine in filters.cuisines,
                        onClick = {
                            val updated = if (cuisine in filters.cuisines) {
                                filters.cuisines - cuisine
                            } else {
                                filters.cuisines + cuisine
                            }
                            onFiltersChange(filters.copy(cuisines = updated))
                        },
                        label = { Text(cuisine.displayName) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Minimum rating: ${String.format("%.1f", filters.minRating)}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = filters.minRating,
                onValueChange = { onFiltersChange(filters.copy(minRating = it)) },
                valueRange = 0f..5f,
                steps = 9,
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Max price: ${formatPriceLevel(filters.maxPriceLevel)}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = filters.maxPriceLevel.toFloat(),
                onValueChange = { onFiltersChange(filters.copy(maxPriceLevel = it.toInt())) },
                valueRange = 1f..4f,
                steps = 2,
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Max distance: ${filters.maxDistanceKm?.let { String.format("%.0f km", it) } ?: "Any"}",
                style = MaterialTheme.typography.titleMedium,
            )
            Slider(
                value = (filters.maxDistanceKm ?: 20.0).toFloat(),
                onValueChange = { onFiltersChange(filters.copy(maxDistanceKm = it.toDouble())) },
                valueRange = 1f..20f,
                steps = 18,
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Sort by", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = filters.sortBy == option,
                        onClick = { onFiltersChange(filters.copy(sortBy = option)) },
                        label = { Text(option.displayName) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        onFiltersChange(FilterOptions())
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Apply")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
