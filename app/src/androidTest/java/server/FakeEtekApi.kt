package server

import com.mozzarelly.rodeoserver.devices.EtekApi
import com.mozzarelly.rodeoserver.devices.toOnText
import retrofit2.Response

class FakeEtekApi : EtekApi {
  private var officeStatus = false

  override suspend fun logIn(body: EtekApi.LoginBody): Response<EtekApi.LoginResponse> =
    Response.success(
      EtekApi.LoginResponse(
      result = EtekApi.LoginResponse.Result(
        token = "token",
        accountID = "accountId"
      ))
    )

  override suspend fun getDevices(body: EtekApi.DevicesBody, accountId: String, token: String): Response<EtekApi.DevicesResponse> =
    Response.success(
      EtekApi.DevicesResponse(
      result = EtekApi.DevicesResponse.Result(
        list = listOf(
          EtekApi.DevicesResponse.DeviceResult(
            deviceName = "office",
            cid = "office",
            deviceStatus = officeStatus.toOnText()
          )
        )
      )
    ))

  override suspend fun toggle(id: String, state: String): Response<Unit> = if (id == "office") {
    officeStatus = state == "on"
    Response.success(Unit)
  } else {
    Response.error(404, null)
  }
}