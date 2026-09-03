@file:OptIn(ExperimentalCoroutinesApi::class)

package server

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.mozzarelly.rodeoserver.AppDatabaseRoom
import com.mozzarelly.rodeoserver.RoomAppDatabase
import com.mozzarelly.rodeoserver.devices.Device
import com.mozzarelly.rodeoserver.devices.DeviceRepositoryImpl
import com.mozzarelly.rodeoserver.devices.EtekDeviceRepository
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
import org.junit.Assert
import org.junit.Before
import org.junit.Test

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

  private val db = RoomAppDatabase(
    Room.inMemoryDatabaseBuilder(
      ApplicationProvider.getApplicationContext(),
      AppDatabaseRoom::class.java
    )
      .allowMainThreadQueries()
      .build()
  )

  private val deviceRepo = DeviceRepositoryImpl(
    deviceDao = db.deviceDao(),
    etek = EtekDeviceRepository(
      credentials = mapOf(Subsystem.Etek to mapOf("etekLogin" to "test", "etekPassword" to "test")),
      deviceDao = db.deviceDao(),
      api = FakeEtekApi()
    )
  )

  private val workQueue = WorkQueue(
    workDao = workDao,
    deviceRepository = deviceRepo,
    connectivity = connectivity
  )

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
      db.deviceDao().addDevice(
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
    db.close()
  }

  @Test
  fun happyPathDeviceToggle() = runTest(testDispatcher) {
    deviceRepo.devices.test {
      Assert.assertEquals(false, awaitItem().find { it.name == "office" }?.isOn)
    }

    advanceUntilIdle()

    updateDevice("office", true, false)

    deviceRepo.devices.test {
      val device = awaitItem().find { it.name == "office" }
      Assert.assertEquals(true, device?.isOn)
      Assert.assertEquals(1, device?.version)
      Assert.assertEquals(false, device?.synced)
    }

    advanceUntilIdle()

    deviceRepo.devices.test {
      val device = awaitItem().find { it.name == "office" }
      Assert.assertEquals(true, device?.isOn)
      Assert.assertEquals(2, device?.version)
      Assert.assertEquals(true, device?.synced)
    }
  }

  @Test
  fun deviceToggleWithNetworkMissing() = runTest(testDispatcher) {
    deviceRepo.devices.test {
      Assert.assertEquals(false, awaitItem().find { it.name == "office" }?.isOn)
    }

    advanceUntilIdle()

    connectivity.networkAvailable = false

    updateDevice("office", true, false)

    deviceRepo.devices.test {
      val device = awaitItem().find { it.name == "office" }
      Assert.assertEquals(true, device?.isOn)
      Assert.assertEquals(false, device?.synced)
    }

    advanceUntilIdle()

    deviceRepo.devices.test {
      val device = awaitItem().find { it.name == "office" }
      Assert.assertEquals(true, device?.isOn)
      Assert.assertEquals(true, device?.synced)
    }
  }
}