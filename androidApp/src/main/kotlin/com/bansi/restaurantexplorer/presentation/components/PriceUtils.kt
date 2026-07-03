package com.bansi.restaurantexplorer.presentation.components

import com.bansi.restaurantexplorer.domain.model.Restaurant

fun Restaurant.formatAveragePrice(): String {
    // Generate a deterministic price based on ID and priceLevel
    val basePrice = when (priceLevel) {
        1 -> 150
        2 -> 450
        3 -> 850
        4 -> 1500
        else -> 200
    }
    
    val variance = (id.hashCode() % 10) * 10 // Adds some "randomness" like 10, 20, 30...
    val price = basePrice + variance
    
    return "₹$price"
}

fun formatPriceLevel(level: Int): String {
    return when (level) {
        1 -> "₹200"
        2 -> "₹500"
        3 -> "₹1000"
        4 -> "₹2000+"
        else -> "₹"
    }
}
