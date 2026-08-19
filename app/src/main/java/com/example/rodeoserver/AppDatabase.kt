package com.example.rodeoserver

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rodeoserver.devices.Device
import com.example.rodeoserver.devices.DeviceDao
import com.example.rodeoserver.devices.Work

@Database(
  entities = [
    Device::class,
    Work::class
  ],
  exportSchema = true,
  version = 2
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun deviceDao(): DeviceDao
}