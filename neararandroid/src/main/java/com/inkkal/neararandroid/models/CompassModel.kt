package com.inkkal.neararandroid.models

internal data class CompassModel(
    val orientationModel: OrientationModel,
    val destinations: List<DestinationModel>,
    val maxDistance: Int,
    val minDistance: Int,
    val currentLocation: LocationModel
)
