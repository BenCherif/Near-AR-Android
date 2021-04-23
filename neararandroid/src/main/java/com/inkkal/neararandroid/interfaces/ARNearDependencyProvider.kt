package com.inkkal.neararandroid.interfaces

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleOwner

interface ARNearDependencyProvider {
    fun getSensorsContext(): Context
    fun getARViewLifecycleOwner(): LifecycleOwner
    fun getPermissionActivity(): Activity
}
