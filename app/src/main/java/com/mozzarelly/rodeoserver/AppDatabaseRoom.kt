package com.mozzarelly.rodeoserver

import androidx.room.Dao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.mozzarelly.rodeoserver.devices.Device
import com.mozzarelly.rodeoserver.devices.DeviceDao
import com.mozzarelly.rodeoserver.work.Work
import com.mozzarelly.rodeoserver.work.WorkDao

/**
 * Room-backed DAOs. They inherit their query methods from the platform-agnostic
 * `:server` contracts; the `@Dao` annotation is what tells Room to generate them.
 */
@Dao
interface RoomDeviceDao : DeviceDao

@Dao
interface RoomWorkDao : WorkDao

@Database(
  entities = [
    Device::class,
    Work::class
  ],
  exportSchema = true,
  version = 2
)
abstract class AppDatabaseRoom : RoomDatabase() {
  abstract fun deviceDao(): RoomDeviceDao
  abstract fun workDao(): RoomWorkDao
}

/** Adapts the Room database to the `:server` [AppDatabase] abstraction. */
class RoomAppDatabase(private val room: AppDatabaseRoom) : AppDatabase {
  override fun deviceDao(): DeviceDao = room.deviceDao()
  override fun workDao(): WorkDao = room.workDao()
  override suspend fun withTransaction(block: suspend () -> Unit) {
    room.withTransaction(block)
  }
  fun close() {
    room.close()
  }
}