package com.Blackbox.muslim.ui

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult as FusedLocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class LocationHelper(private val context: Context) {

    data class LocationData(val latitude: Double, val longitude: Double, val cityName: String, val countryName: String)

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(callback: (LocationData?) -> Unit) {
        try {
            @Suppress("MissingPermission")
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val address = getAddressFromLocation(location)
                    callback(LocationData(location.latitude, location.longitude, address.first, address.second))
                } else {
                    requestNewLocation(callback)
                }
            }.addOnFailureListener {
                callback(null)
            }
        } catch (e: SecurityException) {
            callback(null)
        }
    }

    private fun requestNewLocation(callback: (LocationData?) -> Unit) {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdates(1)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: FusedLocationResult) {
                    super.onLocationResult(result)
                    val location = result.lastLocation
                    if (location != null) {
                        val address = getAddressFromLocation(location)
                        callback(LocationData(location.latitude, location.longitude, address.first, address.second))
                    } else {
                        callback(null)
                    }
                }
            }

            @Suppress("MissingPermission")
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            callback(null)
        }
    }

    private fun getAddressFromLocation(location: Location): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale("ar"))
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: ""
                val country = address.countryName ?: ""
                Pair(city, country)
            } else {
                Pair("", "")
            }
        } catch (e: Exception) {
            Pair("", "")
        }
    }
}
