package com.inkkal.neararandroid.ui

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.inkkal.neararandroid.models.CompassModel
import com.inkkal.neararandroid.repository.CompassRepository
import com.inkkal.neararandroid.models.LocationModel
import com.inkkal.neararandroid.helpers.ViewState
import com.inkkal.neararandroid.interfaces.ARNearVM
import com.inkkal.neararandroid.permissions.PermissionManager
import com.inkkal.neararandroid.permissions.PermissionResult
import javax.inject.Inject


internal class ARNearViewModel @Inject constructor(
    private val compassRepository: CompassRepository,
    private val permissionManager: PermissionManager
) : ARNearVM {

    companion object {
        private const val LOCATION_DATA = "location_data"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error"
    }

    override val permissionState: MutableLiveData<PermissionResult> = MutableLiveData()

    override fun setDestinations(destinations: List<LocationModel>) {
        if (!permissionManager.areAllPermissionsGranted()) checkPermissions()
        compassRepository.destinationsLocation = destinations
    }

    override fun compassState(): LiveData<ViewState<CompassModel>> {
        return compassRepository.getCompassUpdates()
            .map<ViewState<CompassModel>> { ViewState.Success(it) }
            .onErrorReturn { ViewState.Error(it.localizedMessage ?: UNEXPECTED_ERROR_MESSAGE) }
            .toLiveData()
    }

    override fun setLowPassFilterAlpha(lowPassFilterAlpha: Float) {
        compassRepository.setLowPassFilterAlpha(lowPassFilterAlpha)
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        compassRepository.destinationsLocation.let {
            bundle.putParcelableArrayList(LOCATION_DATA, ArrayList(it))
        }
    }

    override fun onRestoreInstanceState(bundle: Bundle) {
        (bundle.get(LOCATION_DATA) as? ArrayList<LocationModel>)?.let {
            compassRepository.destinationsLocation = it
        }
    }

    override fun checkPermissions() {
        if (permissionManager.areAllPermissionsGranted()) {
            permissionState.postValue(
                PermissionResult.GRANTED
            )
        } else permissionManager.requestAllPermissions()
    }

    override fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionResult =
            permissionManager.getPermissionsRequestResult(requestCode, grantResults)
        permissionState.postValue(
            permissionResult
        )
    }
}