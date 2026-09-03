package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.AppDatabase
import com.mozzarelly.rodeoserver.work.WorkQueue
import javax.inject.Inject

class ToggleDeviceUseCase @Inject constructor(
  private val deviceRepository: DeviceRepository,
  private val db: AppDatabase,
  private val workQueue: WorkQueue,
) {
  private val deviceDao = db.deviceDao()

  suspend operator fun invoke(name: String, lock: Boolean) {
    invoke(deviceDao.get(name))
  }

  suspend operator fun invoke(device: Device) {
    db.withTransaction {
      val updated = device.copyWithIncrement(isOn = !device.isOn, synced = false)
      deviceDao.update(updated)
      workQueue.enqueue(deviceRepository.getUpdateWork(updated))
    }

    workQueue.drain()
  }
}
