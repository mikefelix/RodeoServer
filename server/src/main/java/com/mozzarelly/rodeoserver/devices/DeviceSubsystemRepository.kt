package com.mozzarelly.rodeoserver.devices

import androidx.room.withTransaction
import com.mozzarelly.rodeoserver.AppDatabase
import com.mozzarelly.rodeoserver.work.WorkQueue
import com.mozzarelly.rodeoserver.work.WorkResult
import com.mozzarelly.rodeoserver.work.Work
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response

abstract class DeviceSubsystemRepository(
  private val workQueue: WorkQueue,
  private val deviceDao: DeviceDao,
  private val db: AppDatabase
) {
  abstract val devices: List<StateFlow<Device>>

  suspend fun update(device: Device){
    db.withTransaction {
      updateLocal(device)
      workQueue.enqueue(getUpdateWork(device))
    }

    workQueue.drain()
  }

  private fun updateLocal(device: Device) {
    deviceDao.update(device)
  }

  abstract suspend fun updateRemote(device: Device): WorkResult

  protected abstract suspend fun getUpdateWork(device: Device): Work
}

fun <A> Response<A>.toWorkResult(): WorkResult = if (isSuccessful)
  WorkResult.Success
else if (code() in 500..599)
  WorkResult.RetriableFailure
else
  WorkResult.PermanentFailure