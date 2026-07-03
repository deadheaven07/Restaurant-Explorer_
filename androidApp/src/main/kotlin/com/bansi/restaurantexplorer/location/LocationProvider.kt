package com.bansi.restaurantexplorer.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.bansi.restaurantexplorer.domain.model.UserLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    val defaultLocation = UserLocation(latitude = 37.7749, longitude = -122.4194)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): UserLocation? {
        return suspendCancellableCoroutine { continuation ->
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(UserLocation(location.latitude, location.longitude))
                    } else {
                        requestFreshLocation { fresh ->
                            continuation.resume(fresh)
                        }
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(onResult: (UserLocation?) -> Unit) {
        val cancellationToken = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationToken.token)
            .addOnSuccessListener { location ->
                onResult(location?.let { UserLocation(it.latitude, it.longitude) })
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    @SuppressLint("MissingPermission")
    fun observeLocationUpdates(): Flow<UserLocation> = callbackFlow {
        val callback = fusedClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let { trySend(UserLocation(it.latitude, it.longitude)) }
            }
        awaitClose { callback }
    }
}
