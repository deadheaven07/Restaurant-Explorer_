package com.bansi.restaurantexplorer.data.remote

import android.content.Context
import com.bansi.restaurantexplorer.data.remote.dto.RestaurantsResponse
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockRestaurantInterceptor(
    private val context: Context,
    private val gson: Gson,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.encodedPath.endsWith("/restaurants")) {
            Thread.sleep(400)
            val json = context.assets.open("restaurants.json").bufferedReader().use { it.readText() }
            gson.fromJson(json, RestaurantsResponse::class.java)
            return Response.Builder()
                .code(200)
                .message("OK")
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        }
        return chain.proceed(request)
    }
}
