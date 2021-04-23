package com.inkkal.neararandroid.interfaces

import android.os.Bundle
import androidx.lifecycle.LiveData
import com.inkkal.neararandroid.models.CompassModel
import com.inkkal.neararandroid.models.LocationModel
import com.inkkal.neararandroid.helpers.ViewState
import com.inkkal.neararandroid.permissions.PermissionResult


internal interface ARNearVM {
    val permissionState: LiveData<PermissionResult>
    fun compassState(): LiveData<ViewState<CompassModel>>
    fun setDestinations(destinations: List<LocationModel>)
    fun setLowPassFilterAlpha(lowPassFilterAlpha: Float)
    fun onSaveInstanceState(bundle: Bundle)
    fun onRestoreInstanceState(bundle: Bundle)
    fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )

    fun checkPermissions()
}
