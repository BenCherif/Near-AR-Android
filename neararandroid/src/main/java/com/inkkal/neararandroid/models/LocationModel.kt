package com.inkkal.neararandroid.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class LocationModel(val latitude: Double, val longitude: Double, val username: String, val userImage: String) : Parcelable
