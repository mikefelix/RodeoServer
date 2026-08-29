package com.mozzarelly.rodeoserver

import com.mozzarelly.rodeoserver.devices.DeviceDao
import com.mozzarelly.rodeoserver.work.WorkDao

interface AppDatabase {
  fun deviceDao(): DeviceDao
  fun workDao(): WorkDao
  suspend fun withTransaction(block: suspend () -> Unit)
}