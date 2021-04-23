package com.inkkal.neararandroid.models



internal data class ARNearModel(
    val distance: Int,
    val positionX: Float,
    val positionY: Float,
    val alpha: Int,
    val id: Int = 0,
    val username: String,
    val userImage: String
)