package com.inkkal.neararandroid.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.inkkal.neararandroid.R
import com.inkkal.neararandroid.helpers.ViewState
import com.inkkal.neararandroid.interfaces.ARNearDependencyProvider
import com.inkkal.neararandroid.interfaces.ARNearVM
import com.inkkal.neararandroid.interfaces.ARNearViewModel
import com.inkkal.neararandroid.interfaces.DaggerARNearViewModel
import com.inkkal.neararandroid.models.CompassModel
import com.inkkal.neararandroid.models.LocationModel
import com.inkkal.neararandroid.permissions.PermissionResult
import kotlinx.android.synthetic.main.ar_near_layout.view.*
import timber.log.Timber

@Suppress("UnusedPrivateMember", "TooManyFunctions")
class ARNearView : FrameLayout, LifecycleObserver {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    init {
        View.inflate(context, R.layout.ar_near_layout, this)
    }

    private lateinit var viewModel: ARNearVM
    private lateinit var arNearViewModel: ARNearViewModel

    companion object {
        private const val SAVED_STATE = "saved_state"
    }

    fun onCreate(arNearDependencyProvider: ARNearDependencyProvider) {
        arNearViewModel =
            DaggerARNearViewModel.factory().create(arNearDependencyProvider)
        viewModel = arNearViewModel.arNearViewModel()
        arNearViewModel.arNearDependencyProvider().getARViewLifecycleOwner()
            .lifecycle.addObserver(this)
        checkPermissions()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onActivityDestroy() {
        arView.setLowPassFilterAlphaListener(null)
    }

    fun setDestinations(destinations: List<LocationModel>) {
        viewModel.setDestinations(destinations)
    }

    private fun observeCompassState() {
        arView.setLowPassFilterAlphaListener {
            viewModel.setLowPassFilterAlpha(it)
        }
        viewModel.compassState().observe(
            arNearViewModel.arNearDependencyProvider().getARViewLifecycleOwner(),
            { viewState ->
                when (viewState) {
                    is ViewState.Success<CompassModel> -> handleSuccessData(viewState.data)
                    is ViewState.Error -> showErrorDialog(viewState.message)
                }
            })
    }

    private fun checkPermissions() {
        viewModel.permissionState.observe(
            arNearViewModel.arNearDependencyProvider().getARViewLifecycleOwner(),
            { permissionState ->
                when (permissionState) {
                    PermissionResult.GRANTED -> {
                        previewView.post { startCameraPreview() }
                        observeCompassState()
                    }
                    PermissionResult.SHOW_RATIONALE -> showRationaleSnackbar()
                    PermissionResult.NOT_GRANTED -> Unit
                }
            })
        viewModel.checkPermissions()
    }

    private fun handleSuccessData(compassModel: CompassModel) {
        arView.setCompassData(compassModel)
    }

    private fun startCameraPreview() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
            /* .also {
                 it.setSurfaceProvider(previewView.surfaceProvider)
             }*/

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(arNearViewModel.arNearDependencyProvider().getARViewLifecycleOwner(), cameraSelector, preview)
                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (exc: Exception) {
                Timber.e("Use case binding failed$exc")
            }


        }, ContextCompat.getMainExecutor(context))

    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(context)
            .setTitle(R.string.error_title)
            .setMessage(resources.getString(R.string.error_message, message))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                observeCompassState()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->

            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        viewModel.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    private fun showRationaleSnackbar() {
        Snackbar.make(
            this,
            R.string.permissions_not_granted,
            Snackbar.LENGTH_SHORT
        )
            .setAction(R.string.permission_recheck_question) { viewModel.checkPermissions() }
            .setDuration(BaseTransientBottomBar.LENGTH_LONG)
            .show()
    }

    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            putParcelable(SAVED_STATE, super.onSaveInstanceState())
            viewModel.onSaveInstanceState(this)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state
        if (newState is Bundle) {
            viewModel.onRestoreInstanceState(newState)
            newState = newState.getParcelable(SAVED_STATE)
        }
        super.onRestoreInstanceState(newState)
    }
}