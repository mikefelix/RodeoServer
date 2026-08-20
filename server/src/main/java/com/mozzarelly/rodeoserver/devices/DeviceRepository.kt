package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.work.WorkResult
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
      Subsystem.Etek -> etek
      Subsystem.Tuya -> TODO()
      Subsystem.Tasmota -> TODO()
      Subsystem.Wemo -> TODO()
      Subsystem.Shelly -> TODO()
    }

    handler.update(device)
  }

  suspend fun doUpdate(name: String, isOn: Boolean): WorkResult {
    val device = deviceDao.get(name)
    val handler = when (device.subsystem) {
      Subsystem.Etek -> etek
      Subsystem.Tuya -> TODO()
      Subsystem.Tasmota -> TODO()
      Subsystem.Wemo -> TODO()
      Subsystem.Shelly -> TODO()
    }

    return handler.updateRemote(device.copy(isOn = isOn))
  }
}