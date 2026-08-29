package com.mozzarelly.rodeoserver.devices

import com.mozzarelly.rodeoserver.server.CredentialsMap
import com.mozzarelly.rodeoserver.work.Work
import com.mozzarelly.rodeoserver.work.WorkResult
import com.mozzarelly.rodeoserver.work.WorkType
import kotlinx.coroutines.CancellationException
import java.io.IOException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

class EtekDeviceRepository @Inject constructor(
  @Named("credentials") credentials: CredentialsMap,
  deviceDao: DeviceDao,
  private val api: EtekApi
) : DeviceRepository {

  private val creds = credentials.getValue(Subsystem.Etek)

  var remoteDevices: Map<String, EtekApi.DevicesResponse.DeviceResult>? = null

  private var token: String? = null
  private var accountId: String? = null

  override val devices = deviceDao.getSubsystemDevicesFlow(Subsystem.Etek)

  override suspend fun updateRemote(name: String, isOn: Boolean): WorkResult {
    try {
      val res = api.toggle(name, isOn.toOnText())
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

  override suspend fun updateRemote(device: Device): WorkResult {
    return updateRemote(device.name, device.isOn)
  }

  override suspend fun getUpdateWork(device: Device): Work {
    loadRemoteDevices()

    return Work(
      workType = WorkType.ToggleDevice,
      param1 = remoteDevices?.get(device.name)?.cid ?: error("Device not present"),
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

  private suspend fun loadRemoteDevices(){
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

val Response<*>.isRetriable
  get() = code() in 500..599