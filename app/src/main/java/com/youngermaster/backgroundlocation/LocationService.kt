package com.youngermaster.backgroundlocation

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
class LocationService {
    suspend fun getLocation(context: Context): Location? {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val isUserLocationPermissionGranted = true
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled =
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )

        if (!isGPSEnabled || !isUserLocationPermissionGranted) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            fusedLocationProviderClient.lastLocation.apply {
                if (isComplete) {
                    if (isSuccessful) {
                        continuation.resume(result) {}
                    } else {
                        continuation.resume(null){}
                    }
                return@suspendCancellableCoroutine
                }
                if (isCanceled) {
                    continuation.cancel()
                }

                addOnSuccessListener { location ->
                    continuation.resume(location){}
                }

                addOnFailureListener { _ ->
                    continuation.resume(null){}
                }

                addOnCanceledListener { continuation.resume(null){} }
            }
        }
    }
}