package com.example.rodeoserver

import com.example.rodeoserver.devices.Device
import com.example.rodeoserver.devices.DeviceDao
import com.example.rodeoserver.devices.Subsystem.Etek
import com.example.rodeoserver.devices.Subsystem.Shelly
import com.example.rodeoserver.devices.Subsystem.Tasmota
import com.example.rodeoserver.devices.Subsystem.Tuya
import com.example.rodeoserver.devices.Subsystem.Wemo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class DeviceRepository @Inject constructor(
  private val deviceDao: DeviceDao,
  private val etek: EtekDeviceRepository,
){
  val scope = CoroutineScope(SupervisorJob())
  val devices: StateFlow<List<Device>> = deviceDao.getAllDevices()
    .stateIn(scope, SharingStarted.Eagerly, emptyList())

  suspend fun updateDevice(name: String, isOn: Boolean, lock: Boolean) {
    updateDevice(deviceDao.get(name).copy(locked = lock, isOn = isOn))
  }

  suspend fun updateDevice(device: Device) {
    val handler = when (device.subsystem) {
      Etek -> etek
      Tuya -> TODO()
      Tasmota -> TODO()
      Wemo -> TODO()
      Shelly -> TODO()
    }

    handler.update(device)
  }

  suspend fun doUpdate(name: String, isOn: Boolean): WorkResult {
    val device = deviceDao.get(name)
    val handler = when (device.subsystem) {
      Etek -> etek
      Tuya -> TODO()
      Tasmota -> TODO()
      Wemo -> TODO()
      Shelly -> TODO()
    }

    return handler.updateRemote(device.copy(isOn = isOn))
  }
}