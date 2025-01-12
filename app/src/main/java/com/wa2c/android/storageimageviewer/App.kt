package com.wa2c.android.storageimageviewer

import android.app.Application
import com.wa2c.android.storageimageviewer.common.utils.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        Log.init(BuildConfig.DEBUG)
    }
}
