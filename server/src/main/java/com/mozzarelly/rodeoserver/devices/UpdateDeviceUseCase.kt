package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.AppDatabase
import com.mozzarelly.rodeoserver.work.WorkQueue
import javax.inject.Inject

class UpdateDeviceUseCase @Inject constructor(
  private val deviceRepository: DeviceRepository,
  private val db: AppDatabase,
  private val workQueue: WorkQueue,
) {
  private val deviceDao = db.deviceDao()

  suspend operator fun invoke(name: String, isOn: Boolean, lock: Boolean) {
    db.withTransaction {
      val device = deviceDao.get(name)
        .copy(isOn = isOn, synced = false, version = 2)

      deviceDao.update(device)
      workQueue.enqueue(deviceRepository.getUpdateWork(device))
    }

    workQueue.drain()
  }

  suspend operator fun invoke(device: Device) {
    db.withTransaction {
      deviceDao.update(device.copyWithIncrement(synced = false))
      workQueue.enqueue(deviceRepository.getUpdateWork(device))
    }

    workQueue.drain()
  }
}