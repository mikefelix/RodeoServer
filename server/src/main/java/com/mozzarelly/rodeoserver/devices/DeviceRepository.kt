package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.work.Work
import com.mozzarelly.rodeoserver.work.WorkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

interface DeviceRepository {
  val devices: StateFlow<List<Device>>

  suspend fun updateRemote(device: Device): WorkResult<Device>
  suspend fun updateRemote(name: String, isOn: Boolean): WorkResult<Device>
  suspend fun getUpdateWork(device: Device): Work
  suspend fun updateLocal(device: Device)
}

class DeviceRepositoryImpl @Inject constructor(
  private val deviceDao: DeviceDao,
  private val etek: EtekDeviceRepository,
  private val tasmota: TasmotaDeviceRepository,
) : DeviceRepository {

  private val scope = CoroutineScope(SupervisorJob())

  override val devices: StateFlow<List<Device>> = deviceDao.getAllDevices()
    .stateIn(scope, SharingStarted.WhileSubscribed(), initialValue = emptyList())

  override suspend fun getUpdateWork(device: Device): Work {
    val handler = getHandler(device.subsystem)
    return handler.getUpdateWork(device)
  }

  override suspend fun updateRemote(device: Device): WorkResult<Device> {
    return updateRemote(device.name, device.isOn)
  }

  override suspend fun updateRemote(name: String, isOn: Boolean): WorkResult<Device> {
    val device = deviceDao.get(name)
    val handler = getHandler(device.subsystem)
    return handler.updateRemote(device.copy(isOn = isOn))
  }

  override suspend fun updateLocal(device: Device) {
    deviceDao.update(device.copyWithIncrement(synced = true))
  }

  private fun getHandler(subsystem: Subsystem) = when (subsystem) {
    Subsystem.Etek -> etek
    Subsystem.Tasmota -> tasmota
    Subsystem.Tuya -> TODO()
    Subsystem.Wemo -> TODO()
    Subsystem.Shelly -> TODO()
  }

}