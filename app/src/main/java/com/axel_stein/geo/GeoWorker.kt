package com.axel_stein.geo

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture

class GeoWorker(context: Context, params: WorkerParameters) : ListenableWorker(context, params), LocationListener {
    private val tag = "GeoWorker"
    private lateinit var completer: CallbackToFutureAdapter.Completer<Result>
    private lateinit var locationManager: LocationManager

    override fun onLocationChanged(location: Location) {
        Log.d(tag, "$location")
        completer.set(Result.success())
        locationManager.removeUpdates(this)
    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
        completer.set(Result.failure())
        locationManager.removeUpdates(this)
    }

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture {
            completer = it
            if (checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                checkSelfPermission(applicationContext, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
            ) {
                completer.set(Result.failure())
            }

            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val gpsEnabled = locationManager.isProviderEnabled(GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(NETWORK_PROVIDER)
            if (!networkEnabled && !gpsEnabled) {
                completer.set(Result.failure())
            }
            if (gpsEnabled) {
                locationManager.requestLocationUpdates(GPS_PROVIDER, 10_000, 10f, this)
            } else if (networkEnabled) {
                locationManager.requestLocationUpdates(NETWORK_PROVIDER, 10_000, 10f, this)
            }
        }
    }
}