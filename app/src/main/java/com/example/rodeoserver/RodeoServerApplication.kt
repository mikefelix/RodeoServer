package com.example.rodeoserver

import android.app.Application

class RodeoServerApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    OngoingService.startNotification(this)
  }
}