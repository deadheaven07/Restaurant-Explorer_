package com.bansi.restaurantexplorer.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bansi.restaurantexplorer.presentation.bookmarks.BookmarksScreen
import com.bansi.restaurantexplorer.presentation.detail.DetailScreen
import com.bansi.restaurantexplorer.presentation.discovery.DiscoveryScreen
import com.bansi.restaurantexplorer.presentation.components.RequestLocationPermissionEffect
import com.bansi.restaurantexplorer.presentation.navigation.Routes
import com.bansi.restaurantexplorer.presentation.search.SearchScreen

@Composable
fun RestaurantExplorerApp() {
    RequestLocationPermissionEffect()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && !currentRoute.startsWith("detail")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.DISCOVERY,
                        onClick = {
                            navController.navigate(Routes.DISCOVERY) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                        label = { Text("Discover") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SEARCH,
                        onClick = {
                            navController.navigate(Routes.SEARCH) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.BOOKMARKS,
                        onClick = {
                            navController.navigate(Routes.BOOKMARKS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks") },
                        label = { Text("Saved") },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DISCOVERY,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.DISCOVERY) {
                DiscoveryScreen(
                    onRestaurantClick = { id ->
                        navController.navigate(Routes.detail(id))
                    },
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onRestaurantClick = { id ->
                        navController.navigate(Routes.detail(id))
                    },
                )
            }
            composable(Routes.BOOKMARKS) {
                BookmarksScreen(
                    onRestaurantClick = { id ->
                        navController.navigate(Routes.detail(id))
                    },
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument("restaurantId") { type = NavType.StringType },
                ),
            ) {
                DetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
