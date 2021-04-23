package com.inkkal.example

import android.app.Application
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

/**
 * Created by Abderrahim El imame on 11/14/20.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@InstallIn(ApplicationComponent::class)
@Module
object AppModule {

    @Provides
    fun providesFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return FusedLocationProviderClient(app.applicationContext)
    }
}