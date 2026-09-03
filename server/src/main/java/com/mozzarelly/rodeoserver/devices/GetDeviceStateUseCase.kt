package com.mozzarelly.rodeoserver.devices

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeviceStateUseCase @Inject constructor(
  private val deviceRepository: DeviceRepository
) {
  operator fun invoke(): Flow<List<Device>> {
    return deviceRepository.devices
  }
}