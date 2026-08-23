package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.AppDatabase
import com.mozzarelly.rodeoserver.work.WorkQueue

class UpdateDeviceUseCase(
  private val deviceRepository: DeviceRepository,
  private val db: AppDatabase,
  private val workQueue: WorkQueue,
) {
  private val deviceDao = db.deviceDao()

  suspend operator fun invoke(name: String, isOn: Boolean, lock: Boolean) {
    db.withTransaction {
      val device = deviceDao.get(name)
      deviceDao.update(device.copy(isOn = isOn))
      workQueue.enqueue(deviceRepository.getUpdateWork(device))
    }

    workQueue.drain()
  }

  suspend operator fun invoke(device: Device) {
    db.withTransaction {
      deviceDao.update(device)
      workQueue.enqueue(deviceRepository.getUpdateWork(device))
    }

    workQueue.drain()
  }
}