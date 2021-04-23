package com.inkkal.example

import com.google.android.gms.maps.model.LatLng
import com.inkkal.neararandroid.models.LocationModel
import io.reactivex.Single

/**
 * Created by Abderrahim El imame on 11/14/20.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
interface ILocationRepository {
    fun getDestinations(
        destinations: List<LocationModel>
    ): Single<List<LocationModel>>

    fun getCurrentLocation(): Single<LatLng>
}
