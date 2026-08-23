package com.mozzarelly.rodeoserver.server

import com.mozzarelly.rodeoserver.devices.Device
import com.mozzarelly.rodeoserver.devices.DeviceDao
import com.mozzarelly.rodeoserver.devices.Subsystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FakeDeviceDao: DeviceDao {
  private val flow = MutableStateFlow(listOf<Device>())
  private val scope = CoroutineScope(Job())

  override fun getAllDevices(): Flow<List<Device>> = flow

  override fun getSubsystemDevicesFlow(subsystem: Subsystem) = flow.map {
    it.filter { it.subsystem == subsystem }
  }

  override fun getSubsystemDevices(subsystem: Subsystem): List<Device> = getSubsystemDevicesFlow(subsystem)
    .stateIn(scope, SharingStarted.Companion.Eagerly, emptyList())
    .value

  override suspend fun addDevice(device: Device) {
    flow.value += device
  }

  override suspend fun get(name: String): Device = getAllDevices()
    .stateIn(scope, SharingStarted.Companion.Eagerly, emptyList())
    .value.find { it.name == name }
    ?: error("Not found: $name")

  override fun update(device: Device) {
    flow.value = flow.value.map {
      if (it.name == device.name) device else it
    }
  }
}