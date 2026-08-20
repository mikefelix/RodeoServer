package com.mozzarelly.rodeoserver.app

import kotlinx.coroutines.flow.SharedFlow

interface Connectivity {
  val networkReturned: SharedFlow<Unit>
  fun networkAvailable(): Boolean
}