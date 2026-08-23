package com.mozzarelly.rodeoserver

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mozzarelly.rodeoserver.devices.Device
import com.mozzarelly.rodeoserver.devices.DeviceDao
import com.mozzarelly.rodeoserver.work.Work

@Database(
  entities = [
    Device::class,
    Work::class
  ],
  exportSchema = true,
  version = 2
)
abstract class AppDatabaseRoom : RoomDatabase(), AppDatabase {
}