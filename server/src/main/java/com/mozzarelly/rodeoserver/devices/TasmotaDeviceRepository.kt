package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.work.Work
import com.mozzarelly.rodeoserver.work.WorkResult
import com.mozzarelly.rodeoserver.work.WorkType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import javax.inject.Inject

class TasmotaDeviceRepository @Inject constructor(
  private val deviceDao: DeviceDao,
  private val okHttpClient: OkHttpClient,
) : DeviceRepository {
  private val scope = CoroutineScope(SupervisorJob())

  override val devices = deviceDao.getSubsystemDevicesFlow(Subsystem.Tasmota.name)
    .stateIn(scope, SharingStarted.Eagerly, initialValue = emptyList())

  private val apis = devices
    .map {
      it.map { it to makeRetrofit(it.address!!) }
    }
    .onEach {
      it.forEach { (device, api) ->
        api.req(TasmotaApi.Command.Status.command).body()?.let {
          updateLocal(device.copyWithIncrement(isOn = it.isOn))
        }
      }
    }
    .map {
      it.associate { it.first.name to it.second }
    }
    .stateIn(scope, SharingStarted.Eagerly, initialValue = mapOf())

  private suspend fun getState(name: String): TasmotaApi.DeviceResult? {
    val api = apis.value[name]!!
    return try {
      api.req(TasmotaApi.Command.Status.command).body()!!
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (e: Throwable) {
      return null
    }
  }

  override suspend fun updateRemote(name: String, isOn: Boolean): WorkResult<Device> {
    try {
      val api = apis.value[name] ?: error("No API for $name")
      val res = api.req(command = if (isOn) TasmotaApi.Command.On.command else TasmotaApi.Command.Off.command)
      val dev = deviceDao.get(name)
      return res.toWorkResult {
        dev.copy(isOn = it.isOn)
      }
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (_: IOException) {
      return WorkResult.RetriableFailure()
    }
    catch (_: Throwable) {
      return WorkResult.PermanentFailure()
    }
  }

  override suspend fun updateRemote(device: Device): WorkResult<Device> {
    return updateRemote(device.name, device.isOn)
  }

  override suspend fun getUpdateWork(device: Device): Work = Work(
    workType = WorkType.ToggleDevice,
    param1 = device.name,
    param2 = device.isOn.toString(),
  )

  override suspend fun updateLocal(device: Device) {
    deviceDao.update(device.copyWithIncrement(synced = true))
  }

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  private fun makeRetrofit(url: String): TasmotaApi = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl("http://$url")
    .client(okHttpClient)
    .build()
    .create(TasmotaApi::class.java)

}

