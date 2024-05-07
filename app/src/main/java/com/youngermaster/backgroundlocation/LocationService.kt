package com.youngermaster.backgroundlocation

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
class LocationService {
    private val _locationUpdates = MutableStateFlow<Location?>(null)
    val locationUpdates = _locationUpdates.asStateFlow()

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

     fun getLocationContinuous(context: Context) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.create().apply {
            interval = 1000  // Request updates every second
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                _locationUpdates.value = p0.lastLocation
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationUpdates(fusedLocationProviderClient: FusedLocationProviderClient, locationCallback: LocationCallback) {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}