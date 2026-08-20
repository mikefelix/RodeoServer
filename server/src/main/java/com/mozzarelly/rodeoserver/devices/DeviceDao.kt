package com.mozzarelly.rodeoserver.devices

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

interface DeviceDao {

  @Query("select * from device order by name")
  fun getAllDevices(): Flow<List<Device>>

  @Query("select * from device where subsystem = :subsystem order by name")
  fun getSubsystemDevicesFlow(subsystem: Subsystem): Flow<List<Device>>

  @Query("select * from device where subsystem = :subsystem order by name")
  fun getSubsystemDevices(subsystem: Subsystem): List<Device>

  @Upsert
  suspend fun addDevice(device: Device)

  @Query("select * from device where name = :name")
  suspend fun get(name: String): Device

  @Update
  fun update(device: Device)

}


enum class Subsystem {
  Etek, Tuya, Tasmota, Wemo, Shelly
}

@Entity
data class Device(
  @PrimaryKey val name: String,
  val subsystem: Subsystem,
  val isOn: Boolean,
  val locked: Boolean,
  val synced: Boolean
)
