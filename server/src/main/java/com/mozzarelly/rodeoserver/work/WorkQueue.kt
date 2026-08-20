package com.mozzarelly.rodeoserver.work

import com.mozzarelly.rodeoserver.app.Connectivity
import com.mozzarelly.rodeoserver.devices.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
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

  suspend fun drain(){
    mutex.withLock {
      workDao.getAll().forEach {
        val res = when (it.workType) {
          WorkType.ToggleDevice -> deviceRepository.doUpdate(
            name = it.param1!!,
            isOn = it.param2!!.toBoolean(),
          )
        }

        when (res) {
          WorkResult.Success -> workDao.delete(it.id)
          WorkResult.RetriableFailure -> {}
          WorkResult.PermanentFailure -> workDao.delete(it.id)
        }
      }
    }
  }
}

val Response<*>.isRetriable
  get() = code() in 500..599

sealed class WorkResult {
  object Success: WorkResult()
  object RetriableFailure: WorkResult()
  object PermanentFailure: WorkResult()
}