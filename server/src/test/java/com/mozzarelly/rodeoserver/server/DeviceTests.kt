@file:OptIn(ExperimentalCoroutinesApi::class)

package com.mozzarelly.rodeoserver.server

import app.cash.turbine.test
import com.mozzarelly.rodeoserver.AppDatabase
import com.mozzarelly.rodeoserver.devices.Device
import com.mozzarelly.rodeoserver.devices.DeviceDao
import com.mozzarelly.rodeoserver.devices.Subsystem
import com.mozzarelly.rodeoserver.devices.UpdateDeviceUseCase
import com.mozzarelly.rodeoserver.work.Work
import com.mozzarelly.rodeoserver.work.WorkDao
import com.mozzarelly.rodeoserver.work.WorkQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class DeviceTests {
  private val testDispatcher = StandardTestDispatcher()

  private val connectivity = FakeConnectivity()

  private val workDao = object: WorkDao {
    private val flow = MutableStateFlow(listOf<Work>())
    override suspend fun add(work: Work) {
      flow.value += work
    }

    override suspend fun getAll(): List<Work> = flow.value

    override suspend fun delete(id: Long) {
      flow.value = flow.value.filter { it.id != id }
    }
  }

  private val deviceDao = FakeDeviceDao()

  private val deviceRepo = FakeDeviceRepository()

  private val workQueue = WorkQueue(
    workDao = workDao,
    deviceRepository = deviceRepo,
    connectivity = connectivity
  )

/*
  private val etekDeviceRepo = EtekDeviceRepository(
    credentials = mapOf(Subsystem.Etek to mapOf("etekLogin" to "test", "etekPassword" to "test")),
    okHttpClient = OkHttpClient(),
    deviceDao = deviceDao
  )
*/

  private val db = object: AppDatabase {
    override fun deviceDao(): DeviceDao = deviceDao
    override fun workDao(): WorkDao = workDao

    override fun withTransaction(block: suspend () -> Unit) {
      runBlocking {
        block()
      }
    }
  }

  private val updateDevice = UpdateDeviceUseCase(
    deviceRepository = deviceRepo,
    db = db,
    workQueue = workQueue,
  )

  @Before
  fun setup() {
    connectivity.networkAvailable = true
    Dispatchers.setMain(testDispatcher)

    runBlocking {
      deviceDao.addDevice(
        Device(
          name = "office",
          subsystem = Subsystem.Etek,
          isOn = false,
          locked = false,
          synced = false
        )
      )
    }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun happyPathDeviceToggle() = runTest(testDispatcher) {
    deviceRepo.devices.test {
      assertEquals(false, awaitItem().find { it.name == "office" }?.isOn)
    }

    advanceUntilIdle()

    updateDevice("office", true, false)

    deviceRepo.devices.test {
      val device = awaitItem().find { it.name == "office" }
      assertEquals(true, device?.isOn)
      assertEquals(false, device?.synced)
    }

    advanceUntilIdle()

    deviceRepo.devices.test {
      val device = awaitItem().find { it.name == "office" }
      assertEquals(true, device?.isOn)
      assertEquals(true, device?.synced)
    }
  }

  @Test
  fun deviceToggleWithNetworkMissing() = runTest(testDispatcher){

  }
}