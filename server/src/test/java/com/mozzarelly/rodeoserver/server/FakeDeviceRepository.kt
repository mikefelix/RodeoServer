package com.mozzarelly.rodeoserver.server

import com.mozzarelly.rodeoserver.devices.Device
import com.mozzarelly.rodeoserver.devices.DeviceRepository
import com.mozzarelly.rodeoserver.devices.Subsystem
import com.mozzarelly.rodeoserver.devices.toOnText
import com.mozzarelly.rodeoserver.work.Work
import com.mozzarelly.rodeoserver.work.WorkResult
import com.mozzarelly.rodeoserver.work.WorkType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeDeviceRepository: DeviceRepository {
  var fail = false

  override val devices: Flow<List<Device>> = if (fail) flowOf() else flowOf(
    listOf(
      Device("office", Subsystem.Etek, true, false, false)
    )
  )

  override suspend fun updateRemote(device: Device): WorkResult {
    delay(1000)
    return if (fail) WorkResult.RetriableFailure else WorkResult.Success
  }

  override suspend fun updateRemote(name: String, isOn: Boolean): WorkResult {
    delay(1000)
    return if (fail) WorkResult.RetriableFailure else WorkResult.Success
  }

  override suspend fun getUpdateWork(device: Device) = Work(
    workType = WorkType.ToggleDevice,
    param1 = device.name,
    param2 = device.isOn.toOnText()
  )
}