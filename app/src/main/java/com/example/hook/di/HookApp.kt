package com.example.hook.di

import android.app.Application
import com.android.installreferrer.BuildConfig
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class HookApp : Application (){
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())

        }

    }
}