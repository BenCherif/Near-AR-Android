package com.inkkal.neararandroid.di.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.patloew.rxlocation.RxLocation
import com.inkkal.neararandroid.models.LocationModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt

internal class LocationManager(private val rxLocation: RxLocation) {

    companion object {
        private const val LOCATION_REQUEST_INTERVAL = 5000L
        private const val FASTEST_REQUEST_INTERVAL = 20L
        private const val SMALLEST_DISPLACEMENT_NOTICED = 1f
    }

    private val locationRequest = LocationRequest().apply {
        interval = LOCATION_REQUEST_INTERVAL
        fastestInterval = FASTEST_REQUEST_INTERVAL
        smallestDisplacement = SMALLEST_DISPLACEMENT_NOTICED
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flowable<LocationModel> {
        return rxLocation.settings().checkAndHandleResolution(locationRequest)
            .toObservable()
            .flatMap { rxLocation.location().updates(locationRequest) }
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .toFlowable(BackpressureStrategy.LATEST)
            .flatMap { location ->
                Flowable.just(
                    LocationModel(
                        location.latitude,
                        location.longitude,
                        "",
                        ""
                    )
                )
            }
    }

    fun getDistanceBetweenPoints(
        currentLocation: LocationModel?,
        destinationLocation: LocationModel?
    ): Int {
        val locationA = Location("A")

        locationA.latitude = currentLocation?.latitude ?: 0.0
        locationA.longitude = currentLocation?.longitude ?: 0.0

        val locationB = Location("B")

        locationB.latitude = destinationLocation?.latitude ?: 0.0
        locationB.longitude = destinationLocation?.longitude ?: 0.0

        return locationA.distanceTo(locationB).roundToInt()
    }
}
