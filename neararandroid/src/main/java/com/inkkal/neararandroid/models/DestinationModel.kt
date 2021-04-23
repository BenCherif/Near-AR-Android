package com.inkkal.neararandroid.models

internal data class DestinationModel(
    val currentDestinationAzimuth: Float,
    val distanceToDestination: Int,
    val destinationLocation: LocationModel
)
