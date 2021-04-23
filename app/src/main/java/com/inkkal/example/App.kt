package com.inkkal.example

import android.app.Application
import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.components.ApplicationComponent

/**
 * Created by Abderrahim El imame on 11/14/20.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@HiltAndroidApp
class App : Application() {



    companion object {
        lateinit var appContext: Context

        fun get(): App = appContext as App
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}