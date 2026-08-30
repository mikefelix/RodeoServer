package com.mozzarelly.rodeoserver.devices

import kotlinx.coroutines.flow.Flow

class GetDeviceStateUseCase(
  private val deviceRepository: DeviceRepository
) {
  operator fun invoke(): Flow<List<Device>> {
    return deviceRepository.devices
  }
}