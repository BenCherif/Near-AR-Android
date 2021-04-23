package com.inkkal.example

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.google.android.gms.maps.model.LatLng
import com.inkkal.neararandroid.models.LocationModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Created by Abderrahim El imame on 11/14/20.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
class LocationViewModel @ViewModelInject
constructor(
    private val locationRepository: LocationRepository
) :
    ViewModel() {

    val locationLiveData: LiveData<LatLng> =
        locationRepository.getCurrentLocation().toFlowable().toLiveData()
    private val mutableDestinations = MutableLiveData<List<LocationModel>>()
    val destinations: LiveData<List<LocationModel>> = mutableDestinations
    private val mutableViewMode = MutableLiveData<ViewMode>(ViewMode.MapMode)
    val viewMode: LiveData<ViewMode> = mutableViewMode
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val mutableLoading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = mutableLoading


    private fun handleDestinations(destinations: List<LocationModel>) {
        mutableDestinations.postValue(destinations)
    }

    fun arModeClick() {
        mutableViewMode.postValue(ViewMode.ARMode)
    }

    fun mapModeClick() {
        mutableViewMode.postValue(ViewMode.MapMode)
    }

    fun showUsersInTheArea(destinations: List<LocationModel>) {
        mutableLoading.postValue(true)
        locationRepository
            .getDestinations(destinations)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = {
                mutableLoading.postValue(false)
                handleDestinations(it)
            }, onError = {
                mutableLoading.postValue(false)
                it.printStackTrace()

            })
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}