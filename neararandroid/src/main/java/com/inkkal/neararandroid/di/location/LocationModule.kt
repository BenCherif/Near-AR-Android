package com.inkkal.neararandroid.di.location

import com.inkkal.neararandroid.interfaces.ARNearDependencyProvider
import com.patloew.rxlocation.RxLocation
import dagger.Module
import dagger.Provides

@Module
internal class LocationModule {

    @Provides
    internal fun provideLocationProvider(arNearDependencyProvider: ARNearDependencyProvider) =
        LocationManager(RxLocation(arNearDependencyProvider.getSensorsContext()))
}
