package com.mozzarelly.rodeoserver.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RodeoServerApplication : Application() {
  override fun onCreate() {
    super.onCreate()
  }
}