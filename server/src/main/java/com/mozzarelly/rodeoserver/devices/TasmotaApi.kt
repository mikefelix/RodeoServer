package com.mozzarelly.rodeoserver.devices

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.Date

interface TasmotaApi {
  enum class Command(val command: String) {
    Status("Power"),
    On("Power on"),
    Off("Power off"),
  }

  /*
  {
    "Status":{
      "Module":41,
    "DeviceName":"office",
    "FriendlyName":["office"],
    "Topic":"tasmota_952ED5",
    "ButtonTopic":"0",
    "Power":0,
    "PowerOnState":3,
    "LedState":1,
    "LedMask":"FFFF",
    "SaveData":1,
    "SaveState":1,
    "SwitchTopic":"0",
    "SwitchMode":[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    "ButtonRetain":0,
    "SwitchRetain":0,
    "SensorRetain":0,
    "PowerRetain":0,
    "InfoRetain":0,
    "StateRetain":0,
    "StatusRetain":0 }
  }
   */
  @Serializable
  data class DeviceResult(
    @SerialName("POWER") val power: String
  ) {
    val isOn: Boolean
      get() = power.equals("on", ignoreCase = true)
  }

  @GET("cm")
  suspend fun req(@Query("cmnd") command: String): Response<DeviceResult>

}