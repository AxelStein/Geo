package com.axel_stein.geo

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
    }

    private fun requestPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOr(ACCESS_FINE_LOCATION, false) ||
                permissions.getOr(ACCESS_COARSE_LOCATION, false) -> {
                    enqueueGeoWorker()
                }

                else -> {
                    Toast.makeText(this, "Location is not permitted", Toast.LENGTH_SHORT).show()
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun enqueueGeoWorker() {
        val request = OneTimeWorkRequestBuilder<GeoWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager
            .getInstance(this)
            .enqueue(request)
    }
}

fun <K, V> Map<K, V>.getOr(key: K, default: V): V {
    return get(key) ?: default
}