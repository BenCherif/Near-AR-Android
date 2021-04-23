package com.inkkal.neararandroid.di

import com.inkkal.neararandroid.interfaces.ARNearDependencyProvider
import com.inkkal.neararandroid.di.location.LocationModule
import com.inkkal.neararandroid.di.orientation.OrientationModule
import com.inkkal.neararandroid.permissions.PermissionManager
import com.inkkal.neararandroid.ui.ARNearViewModel
import com.inkkal.neararandroid.interfaces.ARNearVM
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        LocationModule::class,
        OrientationModule::class
    ]
)
internal abstract class CompassModule {

    @Binds
    abstract fun provideARNearPresenter(viewModel: ARNearViewModel): ARNearVM

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesPermissionManager(arNearDependencyProvider: ARNearDependencyProvider): PermissionManager {
            return PermissionManager(
                arNearDependencyProvider.getPermissionActivity()
            )
        }
    }
}
