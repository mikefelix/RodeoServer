package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.work.Work
import com.mozzarelly.rodeoserver.work.WorkResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface DeviceRepository {
  val devices: Flow<List<Device>>

  suspend fun updateRemote(device: Device): WorkResult<Device>
  suspend fun updateRemote(name: String, isOn: Boolean): WorkResult<Device>
  suspend fun getUpdateWork(device: Device): Work
  suspend fun updateFromRemote(device: Device)
}

class DeviceRepositoryImpl @Inject constructor(
  private val deviceDao: DeviceDao,
  private val etek: EtekDeviceRepository,
) : DeviceRepository {
//  private val scope = CoroutineScope(SupervisorJob())

  override val devices: Flow<List<Device>> = deviceDao.getAllDevices()

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

  override suspend fun updateFromRemote(device: Device) {
    deviceDao.update(device.copyWithIncrement(synced = true))
  }

  private fun getHandler(subsystem: Subsystem) = when (subsystem) {
    Subsystem.Etek -> etek
    Subsystem.Tuya -> TODO()
    Subsystem.Tasmota -> TODO()
    Subsystem.Wemo -> TODO()
    Subsystem.Shelly -> TODO()
  }

}