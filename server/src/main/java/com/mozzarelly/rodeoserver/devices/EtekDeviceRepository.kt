package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.AppDatabase
import com.mozzarelly.rodeoserver.server.CredentialsMap
import com.mozzarelly.rodeoserver.work.WorkResult
import com.mozzarelly.rodeoserver.work.Work
import com.mozzarelly.rodeoserver.work.WorkType
import com.mozzarelly.rodeoserver.work.WorkQueue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Inject
import javax.inject.Named

class EtekDeviceRepository @Inject constructor(
  @Named("credentials") credentials: CredentialsMap,
  okHttpClient: OkHttpClient,
  workQueue: WorkQueue,
  deviceDao: DeviceDao,
  db: AppDatabase
) : DeviceSubsystemRepository(workQueue, deviceDao, db) {

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  private val creds = credentials.getValue(Subsystem.Etek)

  private val retrofit = Retrofit.Builder()
    .baseUrl("https://smartapi.vesync.com/")
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .client(okHttpClient)
    .build()

  private val api = retrofit.create(EtekApi::class.java)

  var remoteDevices: Map<String, EtekApi.DevicesResponse.DeviceResult>? = null

  private var token: String? = null
  private var accountId: String? = null

  override val devices: List<StateFlow<Device>> = deviceDao.getSubsystemDevices(Subsystem.Etek).map {
    MutableStateFlow(it)
  }

  override suspend fun updateRemote(device: Device): WorkResult {
    try {
      val res = api.toggle(device.name, device.isOn.toOnText())
      return res.toWorkResult()
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (_: IOException) {
      return WorkResult.RetriableFailure
    }
    catch (_: Throwable) {
      return WorkResult.PermanentFailure
    }
  }

  override suspend fun getUpdateWork(device: Device): Work {
    loadDevices()
    val deviceId = remoteDevices?.get(device.name)?.cid ?: error("Device not present")

    return Work(
      workType = WorkType.ToggleDevice,
      param1 = deviceId,
      param2 = device.isOn.toOnText(),
    )
  }

  private suspend fun logIn(force: Boolean = false){
    if (force || token == null) {
      val response = api.logIn(
        EtekApi.LoginBody(
          email = creds.getValue("etekLogin"),
          password = creds.getValue("etekPassword")
        )
      )

      if (response.isSuccessful) {
        this.token = response.body()!!.token
        this.accountId = response.body()!!.accountId
      }
    }
  }

  private suspend fun loadDevices(){
    if (accountId == null || token == null) {
      logIn()
    }

    val accountId = accountId ?: error("Account ID not retrieved.")
    val token = token ?: error("Account token not retrieved.")

    val response = api.getDevices(
      accountId = accountId,
      token = token,
      body = EtekApi.DevicesBody(
        accountId = accountId,
        token = token
      )
    )

    remoteDevices = response.body()?.devices?.associate { it.deviceName to it } ?: return
    devices.forEach {
      (it as MutableStateFlow<Device>).value = remoteDevices?.getValue(it.value.name)!!.toDevice(false, true)
    }
  }
}

fun Boolean.toOnText() = if (this) "on" else "off"

fun EtekApi.DevicesResponse.DeviceResult.toDevice(locked: Boolean, synced: Boolean) = Device(
  name = deviceName,
  subsystem = Subsystem.Etek,
  isOn = isOn,
  locked = locked,
  synced = synced
)