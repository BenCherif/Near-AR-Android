package com.inkkal.neararandroid.interfaces

import com.inkkal.neararandroid.di.CompassModule
import dagger.BindsInstance
import dagger.Component


@Component(
    modules = [
        CompassModule::class
    ]
)
internal interface ARNearViewModel {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance arNearDependencyProvider: ARNearDependencyProvider): ARNearViewModel
    }

    fun arNearViewModel(): ARNearVM
    fun arNearDependencyProvider(): ARNearDependencyProvider
}
