package com.alfredorosas.vet2myfriend

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.location.LocationProvider
import com.alfredorosas.vet2myfriend.domain.location.LocationResult
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

internal class AndroidLocationProvider(context: Context) : LocationProvider {
    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission") // Checked immediately before requesting location; SecurityException is handled.
    override suspend fun currentLocation(): LocationResult {
        val fine = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!fine && !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) return LocationResult.PermissionRequired
        return try {
            withTimeoutOrNull(REQUEST_TIMEOUT_MILLIS) {
                suspendCancellableCoroutine { continuation ->
                    val cancellation = CancellationTokenSource()
                    continuation.invokeOnCancellation { cancellation.cancel() }
                    val request = CurrentLocationRequest.Builder()
                        .setPriority(if (fine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setMaxUpdateAgeMillis(MAX_LOCATION_AGE_MILLIS)
                        .setDurationMillis(FIX_TIMEOUT_MILLIS)
                        .build()
                    client.getCurrentLocation(request, cancellation.token)
                        .addOnSuccessListener { location ->
                            if (continuation.isActive) {
                                continuation.resume(if (location == null) LocationResult.Unavailable else
                                    LocationResult.Success(GeoPoint(location.latitude, location.longitude), location.accuracy))
                            }
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(LocationResult.Unavailable)
                        }
                        .addOnCanceledListener {
                            if (continuation.isActive) continuation.resume(LocationResult.Unavailable)
                        }
                }
            } ?: LocationResult.Unavailable
        } catch (_: SecurityException) {
            LocationResult.PermissionRequired
        }
    }

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED
}

// Foreground UX choices: accept a recent fix, allow 15 seconds for a new one,
// and release the caller after 20 seconds even if Play services does not respond.
private const val MAX_LOCATION_AGE_MILLIS = 10_000L
private const val FIX_TIMEOUT_MILLIS = 15_000L
private const val REQUEST_TIMEOUT_MILLIS = 20_000L
