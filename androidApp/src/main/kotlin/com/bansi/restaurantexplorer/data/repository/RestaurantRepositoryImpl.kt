package com.bansi.restaurantexplorer.data.repository

import com.bansi.restaurantexplorer.data.local.dao.RestaurantDao
import com.bansi.restaurantexplorer.data.local.dao.SearchHistoryDao
import com.bansi.restaurantexplorer.data.local.dao.ViewHistoryDao
import com.bansi.restaurantexplorer.data.local.entity.RestaurantEntity
import com.bansi.restaurantexplorer.data.local.entity.SearchHistoryEntity
import com.bansi.restaurantexplorer.data.local.entity.ViewHistoryEntity
import com.bansi.restaurantexplorer.data.mapper.toDomain
import com.bansi.restaurantexplorer.data.mapper.toEntity
import com.bansi.restaurantexplorer.data.remote.api.RestaurantApi
import com.bansi.restaurantexplorer.domain.model.FilterOptions
import com.bansi.restaurantexplorer.domain.model.Restaurant
import com.bansi.restaurantexplorer.domain.model.SortOption
import com.bansi.restaurantexplorer.domain.model.UserLocation
import com.bansi.restaurantexplorer.domain.model.UserPreferences
import com.bansi.restaurantexplorer.domain.repository.RestaurantRepository
import com.bansi.restaurantexplorer.domain.usecase.RecommendationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepositoryImpl @Inject constructor(
    private val restaurantDao: RestaurantDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val viewHistoryDao: ViewHistoryDao,
    private val restaurantApi: RestaurantApi,
) : RestaurantRepository {

    override fun observeRestaurants(
        filters: FilterOptions,
        location: UserLocation?,
    ): Flow<List<Restaurant>> {
        return observeDataAndPreferences().map { (entities, preferences) ->
            processRestaurants(entities, filters, location, preferences)
        }
    }

    override fun observeBookmarkedRestaurants(location: UserLocation?): Flow<List<Restaurant>> {
        return combine(
            restaurantDao.observeBookmarked(),
            searchHistoryDao.observeRecent(),
            viewHistoryDao.observeAll(),
        ) { bookmarked, searches, views ->
            val domainBookmarked = bookmarked.map { it.toDomain() }
            val preferences = buildPreferences(searches, views, domainBookmarked)
            
            val enriched = RecommendationEngine.enrich(domainBookmarked, location, preferences)
            RecommendationEngine.sort(enriched, SortOption.RATING)
        }
    }

    override fun observeSearchResults(
        query: String,
        filters: FilterOptions,
        location: UserLocation?,
    ): Flow<List<Restaurant>> {
        return observeDataAndPreferences().map { (entities, preferences) ->
            val processed = processRestaurants(entities, filters, location, preferences)
            RecommendationEngine.search(processed, query)
        }
    }

    override fun observeRecommendedRestaurants(location: UserLocation?): Flow<List<Restaurant>> {
        return observeDataAndPreferences().map { (entities, preferences) ->
            val enriched = RecommendationEngine.enrich(
                entities.map { it.toDomain() },
                location,
                preferences,
            )
            RecommendationEngine.sort(enriched, SortOption.RECOMMENDED).take(8)
        }
    }

    override suspend fun getRestaurantById(id: String): Restaurant? {
        return restaurantDao.getById(id)?.toDomain()
    }

    override suspend fun toggleBookmark(restaurantId: String) {
        val current = restaurantDao.getById(restaurantId) ?: return
        restaurantDao.updateBookmark(restaurantId, !current.isBookmarked)
    }

    override suspend fun recordSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        
        searchHistoryDao.deleteQuery(trimmed)
        searchHistoryDao.insert(
            SearchHistoryEntity(
                query = trimmed,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun recordView(restaurantId: String) {
        viewHistoryDao.recordView(restaurantId, System.currentTimeMillis())
    }

    override suspend fun refreshRestaurants() {
        if (restaurantDao.count() == 0) {
            syncFromNetwork(force = true)
            return
        }
        syncFromNetwork(force = false)
    }

    override suspend fun getUserPreferences(): UserPreferences {
        val searches = searchHistoryDao.observeRecent().first()
        val views = viewHistoryDao.observeAll().first()
        val restaurants = restaurantDao.observeAll().first().map { it.toDomain() }
        
        return buildPreferences(searches, views, restaurants)
    }

    override fun observeRecentSearches(): Flow<List<String>> {
        return searchHistoryDao.observeRecent().map { entries -> entries.map { it.query } }
    }

    private fun observeDataAndPreferences(): Flow<RepositoryData> {
        return combine(
            restaurantDao.observeAll(),
            searchHistoryDao.observeRecent(),
            viewHistoryDao.observeAll(),
        ) { restaurants, searches, views ->
            val domainRestaurants = restaurants.map { it.toDomain() }
            val preferences = buildPreferences(searches, views, domainRestaurants)
            RepositoryData(restaurants, preferences)
        }
    }

    private fun processRestaurants(
        entities: List<RestaurantEntity>,
        filters: FilterOptions,
        location: UserLocation?,
        preferences: UserPreferences,
    ): List<Restaurant> {
        val enriched = RecommendationEngine.enrich(
            entities.map { it.toDomain() },
            location,
            preferences,
        )
        val filtered = RecommendationEngine.applyFilters(enriched, filters)
        return RecommendationEngine.sort(filtered, filters.sortBy)
    }

    private fun buildPreferences(
        searches: List<SearchHistoryEntity>,
        views: List<ViewHistoryEntity>,
        restaurants: List<Restaurant>,
    ): UserPreferences {
        val viewedIds = views.associate { it.restaurantId to it.viewCount }
        val bookmarkedCuisines = restaurants.filter { it.isBookmarked }.map { it.cuisine }
        val preferred = RecommendationEngine.derivePreferredCuisines(restaurants, viewedIds, bookmarkedCuisines)
        
        return UserPreferences(
            preferredCuisines = preferred,
            recentSearches = searches.map { it.query },
            viewedRestaurantIds = viewedIds,
        )
    }

    private suspend fun syncFromNetwork(force: Boolean) {
        try {
            val response = restaurantApi.getRestaurants()
            val existing = if (force) emptyMap() else restaurantDao.observeAll().first().associateBy { it.id }
            val timestamp = System.currentTimeMillis()
            
            val entities = response.restaurants.map { dto ->
                val bookmarked = existing[dto.id]?.isBookmarked ?: false
                dto.toEntity(isBookmarked = bookmarked, lastUpdated = timestamp)
            }
            restaurantDao.upsertAll(entities)
        } catch (_: Exception) {
            // Offline-first: cached data remains available if sync fails.
        }
    }

    private data class RepositoryData(
        val entities: List<RestaurantEntity>,
        val preferences: UserPreferences,
    )
}
