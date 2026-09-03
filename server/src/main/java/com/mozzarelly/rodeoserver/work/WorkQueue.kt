package com.mozzarelly.rodeoserver.work

import com.mozzarelly.rodeoserver.app.Connectivity
import com.mozzarelly.rodeoserver.devices.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkQueue @Inject constructor(
  private val workDao: WorkDao,
  private val deviceRepository: DeviceRepository,
  private val connectivity: Connectivity,
) {
  private val mutex = Mutex()
  private val scope = CoroutineScope(SupervisorJob())

  init {
    scope.launch {
      connectivity.networkReturned.collect {
        drain()
      }
    }
  }

  suspend fun enqueue(work: Work) {
    workDao.add(work)
    drain()
  }

  suspend fun drain() {
    mutex.withLock {
      workDao.getAll().forEach { work ->
        val res = when (work.workType) {
          WorkType.ToggleDevice -> deviceRepository.updateRemote(
            name = work.param1!!,
            isOn = work.param2!!.toBoolean(),
          )
        }

        when (res) {
          is WorkResult.Success -> {
            workDao.delete(work.id)
            if (work.workType == WorkType.ToggleDevice)
              deviceRepository.updateLocal(res.result)
          }
          is WorkResult.RetriableFailure -> {}
          is WorkResult.PermanentFailure -> workDao.delete(work.id)
        }
      }
    }
  }
}

sealed class WorkResult<T> {
  class Success<T>(val result: T): WorkResult<T>()
  class RetriableFailure<T>: WorkResult<T>()
  class PermanentFailure<T>: WorkResult<T>()
}