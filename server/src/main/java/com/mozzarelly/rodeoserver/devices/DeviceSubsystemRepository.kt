package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.work.WorkResult
import retrofit2.Response

/*
abstract class DeviceSubsystemRepository(
  private val deviceDao: DeviceDao,
  private val db: AppDatabase
) : DeviceRepository {
  abstract override val devices: List<StateFlow<Device>>

  suspend fun update(device: Device){
    db.withTransaction {
      updateLocal(device)
      workQueue.enqueue(getUpdateWork(device))
    }

    workQueue.drain()
  }

  fun updateLocal(device: Device) {
    deviceDao.update(device)
  }

  abstract suspend fun updateRemote(device: Device): WorkResult

  protected abstract suspend fun getUpdateWork(device: Device): Work
}
*/

fun <A> Response<A>.toWorkResult(): WorkResult = if (isSuccessful)
  WorkResult.Success
else if (code() in 500..599)
  WorkResult.RetriableFailure
else
  WorkResult.PermanentFailure