package com.mozzarelly.rodeoserver.devices

import kotlinx.serialization.SerialName
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import java.util.Date

interface EtekApi {
  data class LoginBody(
    val email: String,
    val password: String, // is actually hex digest of md5 hash of utf8 encoded real password
    val timeZone: String = "America/Denver",
    val acceptLanguage: String = "en",
    val appVersion: String = "2.8.6",
    val phoneBrand: String = "SM N9005",
    val phoneOS: String = "Android",
    val method: String = "login",
    val userType: String = "1",
    val devToken: String = "",
    val traceId: String = Date().time.toString()
  )

  data class LoginResponse(
    val result: Result? = null,
  ) {
    data class Result (
      val token: String? = null,
      val accountID: String? = null,
    )

    val token = result?.token
    val accountId = result?.accountID
  }

  data class DevicesBody(
    @SerialName("accountID") val accountId: String,
    val token: String,
    val timeZone: String = "America/Denver",
    val acceptLanguage: String = "en",
    val appVersion: String = "2.8.6",
    val phoneBrand: String = "SM N9005",
    val phoneOS: String = "Android",
    val method: String = "devices",
    val pageNo: String = "1",
    val traceId: String = Date().time.toString()
  )

  data class DevicesResponse(
    val result: Result? = null,
  ) {
    data class Result (
      val list: List<DeviceResult>,
    )

    data class DeviceResult(
      val deviceName: String,
      val cid: String,
      val deviceStatus: String
    ) {
      val isOn = deviceStatus == "on"
    }

    val devices = result?.list
  }

  @POST("cloud/v1/user/login")
  suspend fun logIn(body: LoginBody): Response<LoginResponse>

  @POST("cloud/v1/deviceManaged/devices")
  @Headers(
    "Content-Type: application/json; charset=UTF-8",
    "User-Agent: okhttp/3.12.1",
  )
  suspend fun getDevices(body: DevicesBody,
                         @Header("accountID") accountId: String,
                         @Header("token") token: String,
  ): Response<DevicesResponse>

  @PUT("v1/wifi-switch-1.3/{deviceId}/status/{state}")
  suspend fun toggle(id: String, state: String): Response<Unit>
}