package com.inkkal.example

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnStart
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Observer
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.inkkal.example.GlideApp
import com.inkkal.example.R
import com.inkkal.example.databinding.ActivityMainBinding
import com.inkkal.neararandroid.getRoundedCornerBitmap
import com.inkkal.neararandroid.interfaces.ARNearDependencyProvider
import com.inkkal.neararandroid.models.LocationModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ARNearDependencyProvider {

    private val locationViewModel by viewModels<LocationViewModel>()

    private lateinit var googleMap: GoogleMap
    private val markers: MutableList<Marker> = mutableListOf()
    private var changeModeTransitionAnimation = false

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.arNear.onCreate(this)
        binding.mapView.addToLifecycle(lifecycle, savedInstanceState)

        setupButtons()
        setupToolbar()
        requestLocationPermission()
    }

    private fun setupToolbar() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.toolbarTitle.text = getString(R.string.app_name)

    }

    private fun setupButtons() {
        binding.arViewIcon.setOnClickListener {
            changeModeTransitionAnimation = true
            locationViewModel.arModeClick()
        }

        binding.backToMapButton.setOnClickListener {
            changeModeTransitionAnimation = true
            locationViewModel.mapModeClick()
        }

        binding.findUsersButton.setOnClickListener {
            //locationViewModel.showUsersInTheArea(destinations)
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        binding.arNear.onRequestPermissionResult(requestCode, permissions, grantResults)
        val locationPermissionIndex = permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (locationPermissionIndex != -1
            && grantResults[locationPermissionIndex] == PackageManager.PERMISSION_GRANTED
        ) {
            initMap()
        }
    }

    private fun initMap() {
        binding.mapView.getMapAsync { googleMap ->
            this.googleMap = googleMap
            setupObservers()
            styleMap(googleMap)
        }
    }

    private fun setupObservers() {
        locationViewModel.locationLiveData.observe(this, Observer { location ->
            moveToLocation(location)
        })
        locationViewModel.destinations.observe(this, Observer { destinations ->
            binding.arNear.setDestinations(destinations)
            showDestinationsOnMap(destinations)
        })
        locationViewModel.viewMode.observe(this, Observer { viewMode ->
            when (viewMode) {
                ViewMode.ARMode -> handleArMode()
                ViewMode.MapMode -> handleMapMode()
            }
        })
        locationViewModel.loading.observe(this, Observer {
            googleMap.uiSettings.isScrollGesturesEnabled = !it
            binding.loadingProgress.isVisible = it
        })

        locationViewModel.showUsersInTheArea(destinations)
    }

    private fun showDestinationsOnMap(destinations: List<LocationModel>) {
        if (markers.isNotEmpty()) removeMarkers()
        /*   val latLngDestinations = destinations
               .map { LatLng(it.latitude, it.longitude) }*/

        destinations
            .forEach {
                GlideApp.with(this)
                    .asBitmap()
                    .load(it.userImage)
                    //.apply(RequestOptions().circleCrop())
                    .into(object : CustomTarget<Bitmap>() {
                        @Override
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {

                            val scale: Float = resources.displayMetrics.density
                            val pixels = (36 * scale + 0.5f).toInt()
                            val bitmap: Bitmap =
                                Bitmap.createScaledBitmap(resource, pixels, pixels, true)
                            addMarker(LatLng(it.latitude, it.longitude), bitmap.getRoundedCornerBitmap(this@MainActivity,36), it.username)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            val bitmap =
                                getDrawable(
                                    R.drawable.default_avatar,
                                    this@MainActivity
                                )?.let { it1 ->
                                    val scale: Float = resources.displayMetrics.density
                                    val pixels = (36 * scale + 0.5f).toInt()
                                    convertToBitmap(
                                        it1,
                                        pixels,
                                        pixels
                                    )
                                }

                            addMarker(LatLng(it.latitude, it.longitude), bitmap?.getRoundedCornerBitmap(this@MainActivity,36), it.username)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                    })

                moveToLocation(LatLng(it.latitude, it.longitude))
            }
    }

    private fun removeMarkers() {
        markers.forEach {
            it.remove()
        }
        markers.clear()
    }

    private fun handleMapMode() {
        setMapGroupVisibility(true)
        setARGroupVisibility(false)
    }

    private fun handleArMode() {
        setMapGroupVisibility(false)
        setARGroupVisibility(true)
    }

    private fun styleMap(googleMap: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        googleMap.apply {
            isMyLocationEnabled = true
            uiSettings?.isMyLocationButtonEnabled = false
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this@MainActivity,
                    R.raw.maps_style
                )
            )
        }
    }

    private fun setARGroupVisibility(show: Boolean) {
        binding.backToMapButton.isVisible = show
        animateView(show, binding.arNear)
    }

    private fun setMapGroupVisibility(show: Boolean) {
        binding.arViewIcon.isVisible = show
        //  binding.findUsersButton.isVisible = show

        animateView(show, binding.mapView)
    }

    private fun animateView(show: Boolean, view: View) {
        if (changeModeTransitionAnimation) {
            if (show) {
                view.alpha = 0f
                view.isVisible = true
            }
            view.animate()
                .setDuration(VIEW_MODE_TRANSITION_ANIMATION_DURATION)
                .alpha(if (show) 1.0f else 0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        view.isVisible = show
                    }
                })
        } else {
            view.isVisible = show
        }
    }

    private fun moveToLocation(location: LatLng) {
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                MOVE_TO_LOCATION_ZOOM
            )
        )
    }

    private fun addMarker(
        position: LatLng,
        bitmap: Bitmap?,
        username: String
    ) {
        val markerOptions = MarkerOptions()
            .apply {
                title(username)
                if (bitmap != null)
                    icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                else
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                position(position)
                visible(true)
            }


        val pinnedMarker = googleMap.addMarker(markerOptions)
        pinnedMarker.showInfoWindow()
        markers.add(pinnedMarker)
        startDropMarkerAnimation(pinnedMarker, googleMap)
    }

    private fun startDropMarkerAnimation(
        pinnedMarker: Marker,
        map: GoogleMap
    ) {
        val target = pinnedMarker.position
        val projection = map.projection
        val targetPoint = projection.toScreenLocation(target)
        val animationDuration =
            (DROP_MARKER_ANIMATION_DURATION_INIT_VALUE + targetPoint.y * DROP_MARKER_ANIMATION_DURATION_FACTOR).toLong()
        val startPoint = projection.toScreenLocation(pinnedMarker.position)
        startPoint.y -= DROP_MARKER_ANIMATION_POSITION_Y_OFFSET
        val startLatLng = projection.fromScreenLocation(startPoint)

        val propertyLatitude = PropertyValuesHolder.ofFloat(
            LATITUDE_ANIMATION_PROPERTY, startLatLng.latitude.toFloat(),
            target.latitude.toFloat()
        )

        val propertyLongitude = PropertyValuesHolder.ofFloat(
            LONGITUDE_ANIMATION_PROPERTY, startLatLng.longitude.toFloat(),
            target.longitude.toFloat()
        )

        prepareDropMarkerAnimation(
            animationDuration,
            propertyLatitude,
            propertyLongitude,
            pinnedMarker
        ).start()
    }

    private fun prepareDropMarkerAnimation(
        animationDuration: Long,
        propertyLatitude: PropertyValuesHolder?,
        propertyLongitude: PropertyValuesHolder?,
        pinnedMarker: Marker
    ): ValueAnimator {
        return ValueAnimator().apply {
            duration = abs(animationDuration)
            interpolator = LinearOutSlowInInterpolator()
            startDelay = DROP_MARKER_ANIMATION_DELAY
            setValues(propertyLatitude, propertyLongitude)
            doOnStart { pinnedMarker.isVisible = true }
            addUpdateListener { animation ->
                val latitude =
                    animation.getAnimatedValue(LATITUDE_ANIMATION_PROPERTY) as? Float ?: 0f
                val longitude =
                    animation.getAnimatedValue(LONGITUDE_ANIMATION_PROPERTY) as? Float ?: 0f
                pinnedMarker.position = LatLng(latitude.toDouble(), longitude.toDouble())
            }
        }
    }

    override fun getSensorsContext() = this
    override fun getARViewLifecycleOwner() = this
    override fun getPermissionActivity() = this

    companion object {
        private const val VIEW_MODE_TRANSITION_ANIMATION_DURATION = 500L
        private const val MOVE_TO_LOCATION_ZOOM = 13f
        private const val LATITUDE_ANIMATION_PROPERTY = "latitude_property"
        private const val LONGITUDE_ANIMATION_PROPERTY = "longitude_property"
        private const val DROP_MARKER_ANIMATION_POSITION_Y_OFFSET = 100
        private const val DROP_MARKER_ANIMATION_DURATION_INIT_VALUE = 200
        private const val DROP_MARKER_ANIMATION_DURATION_FACTOR = 0.6
        private const val DROP_MARKER_ANIMATION_DELAY = 500L
        private const val LOCATION_PERMISSION_REQUEST = 456

        private var destinations: MutableList<LocationModel> = listOf(
            LocationModel(
                31.6605983,
                -8.003544,
                "Dr. Bencherif",""//,
                //"https://i.imgur.com/FbI4HDE.jpg"
            ),
            LocationModel(
                31.6408888,
                -8.0054977,
                "Dr. Tonton",
                "https://i.imgur.com/QGAWR56.jpg"
            )
        ).toMutableList()


        fun show(
            context: Context,
            destinations: MutableList<LocationModel>
        ) {
            Companion.destinations = destinations
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run {
                context.startActivity(this)
            }
        }
    }
}
