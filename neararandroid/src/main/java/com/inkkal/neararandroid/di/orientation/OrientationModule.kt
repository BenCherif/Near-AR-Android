package com.inkkal.neararandroid.di.orientation

import android.hardware.SensorManager
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.inkkal.neararandroid.interfaces.ARNearDependencyProvider
import dagger.Module
import dagger.Provides

@Module
internal class OrientationModule {

    @Provides
    internal fun provideOrientationProvider(
        sensorManager: SensorManager,
        windowManager: WindowManager
    ) = OrientationManager(sensorManager, windowManager)


    @Provides
    internal fun provideSensorManager(arNearDependencyProvider: ARNearDependencyProvider) =
        requireNotNull(arNearDependencyProvider.getSensorsContext().getSystemService<SensorManager>())


    @Provides
    internal fun providesWindowManager(arNearDependencyProvider: ARNearDependencyProvider) =
        requireNotNull(arNearDependencyProvider.getSensorsContext().getSystemService<WindowManager>())
}
