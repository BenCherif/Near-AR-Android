package com.inkkal.neararandroid.repository

import com.inkkal.neararandroid.models.LocationModel
import com.inkkal.neararandroid.di.location.LocationManager
import com.inkkal.neararandroid.models.CompassModel
import com.inkkal.neararandroid.models.DestinationModel
import com.inkkal.neararandroid.di.orientation.OrientationManager
import io.reactivex.Flowable
import io.reactivex.rxkotlin.combineLatest
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal class CompassRepository @Inject constructor(
    private val orientationManager: OrientationManager,
    private val locationManager: LocationManager
) {

    companion object {
        private const val MAXIMUM_ANGLE = 360
    }

    var destinationsLocation: List<LocationModel> = listOf()

    //TODO rework of the compassUpdates
    fun getCompassUpdates(): Flowable<CompassModel> {
        return locationManager
            .getLocationUpdates()
            .combineLatest(orientationManager.getSensorUpdates())
            .flatMap { (currentLocation, currentOrientation) ->
                val destinations = destinationsLocation
                    .map {
                        handleDestination(
                            currentLocation,
                            it,
                            currentOrientation.currentAzimuth
                        )
                    }
                val compassData = CompassModel(
                    currentOrientation,
                    destinations,
                    getMaxDistance(destinations),
                    getMinDistance(destinations),
                    currentLocation
                )
                Flowable.just(compassData)
            }
    }

    private fun getMaxDistance(destinations: List<DestinationModel>) =
        destinations.maxByOrNull { it.distanceToDestination }?.distanceToDestination ?: 0

    private fun getMinDistance(destinations: List<DestinationModel>) =
        destinations.minByOrNull { it.distanceToDestination }
            ?.distanceToDestination ?: 0

    private fun handleDestination(
        currentLocation: LocationModel,
        destinationLocation: LocationModel,
        currentAzimuth: Float
    ): DestinationModel {
        val headingAngle = calculateHeadingAngle(currentLocation, destinationLocation)

        val currentDestinationAzimuth =
            (headingAngle - currentAzimuth + MAXIMUM_ANGLE) % MAXIMUM_ANGLE

        val distanceToDestination = locationManager.getDistanceBetweenPoints(
            currentLocation,
            destinationLocation
        )

        return DestinationModel(
            currentDestinationAzimuth,
            distanceToDestination,
            destinationLocation
        )
    }

    private fun calculateHeadingAngle(currentLocation: LocationModel, destinationLocation: LocationModel): Float {
        val currentLatitudeRadians = Math.toRadians(currentLocation.latitude)
        val destinationLatitudeRadians = Math.toRadians(destinationLocation.latitude)
        val deltaLongitude = Math.toRadians(destinationLocation.longitude - currentLocation.longitude)

        val y = cos(currentLatitudeRadians) * sin(destinationLatitudeRadians) -
                sin(currentLatitudeRadians) * cos(destinationLatitudeRadians) * cos(deltaLongitude)
        val x = sin(deltaLongitude) * cos(destinationLatitudeRadians)
        val headingAngle = Math.toDegrees(atan2(x, y)).toFloat()

        return (headingAngle + MAXIMUM_ANGLE) % MAXIMUM_ANGLE
    }

    fun setLowPassFilterAlpha(lowPassFilterAlpha: Float) {
        orientationManager.setLowPassFilterAlpha(lowPassFilterAlpha)
    }
}
