package com.bansi.restaurantexplorer.di

import android.content.Context
import androidx.room.Room
import com.bansi.restaurantexplorer.data.local.RestaurantDatabase
import com.bansi.restaurantexplorer.data.local.dao.RestaurantDao
import com.bansi.restaurantexplorer.data.local.dao.SearchHistoryDao
import com.bansi.restaurantexplorer.data.local.dao.ViewHistoryDao
import com.bansi.restaurantexplorer.data.remote.MockRestaurantInterceptor
import com.bansi.restaurantexplorer.data.remote.api.RestaurantApi
import com.bansi.restaurantexplorer.data.repository.RestaurantRepositoryImpl
import com.bansi.restaurantexplorer.domain.repository.RestaurantRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RestaurantDatabase {
        return Room.databaseBuilder(
            context,
            RestaurantDatabase::class.java,
            "restaurant_explorer.db",
        ).build()
    }

    @Provides
    fun provideRestaurantDao(database: RestaurantDatabase): RestaurantDao = database.restaurantDao()

    @Provides
    fun provideSearchHistoryDao(database: RestaurantDatabase): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    fun provideViewHistoryDao(database: RestaurantDatabase): ViewHistoryDao = database.viewHistoryDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        gson: Gson,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(MockRestaurantInterceptor(context, gson))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.restaurantexplorer.local/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideRestaurantApi(retrofit: Retrofit): RestaurantApi {
        return retrofit.create(RestaurantApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(
        impl: RestaurantRepositoryImpl,
    ): RestaurantRepository
}
