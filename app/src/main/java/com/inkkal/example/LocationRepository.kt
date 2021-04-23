package com.inkkal.example

import android.annotation.SuppressLint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.inkkal.neararandroid.models.LocationModel
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Abderrahim El imame on 11/14/20.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@Singleton
class LocationRepository
@Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : ILocationRepository {

    companion object {
        private const val LOCATION_ERROR = "Failed to get current location"
    }

    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(): Single<LatLng> {
        return Single.create { emitter ->
            fusedLocationProviderClient
                .lastLocation.addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.let { location ->
                            emitter.onSuccess(LatLng(location.latitude, location.longitude))
                        }
                    } else {
                        emitter.onError(Throwable(LOCATION_ERROR))
                    }
                }
        }
    }

    override fun getDestinations(
        destinations: List<LocationModel>
    ): Single<List<LocationModel>> {

        return Single.just(destinations
            .map { locationNode ->
                LocationModel(locationNode.latitude, locationNode.longitude,locationNode.username,locationNode.userImage)
            })


    }
}