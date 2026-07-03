package com.bansi.restaurantexplorer.presentation.navigation

object Routes {
    const val DISCOVERY = "discovery"
    const val SEARCH = "search"
    const val BOOKMARKS = "bookmarks"
    const val DETAIL = "detail/{restaurantId}"

    fun detail(restaurantId: String) = "detail/$restaurantId"
}
