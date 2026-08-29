package com.mozzarelly.rodeoserver.server

import com.mozzarelly.rodeoserver.devices.UpdateDeviceUseCase
import io.ktor.server.application.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.delete
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail

/*
'GET /devicedefs': ()
'GET /devices': ()
'GET /devicegroups': async ()
'POST /device/([a-z0-9]+)/([0-9]+)-([0-9]+)-([0-9]+)': async (request, device, hue, bri, sat)
'POST /device/([a-z0-9]+)(/[a-z0-9]+)?(/[a-z0-9]+)?': async (request, device, arg1, arg2)
'PUT /device/([a-z0-9]+)(/[a-z]+)?': async (request, device, lock)
'DELETE /device/([a-z0-9]+)(/[a-z]+)?': async (request, device, lock)
'PUT /watch/([0-9a-z]+)/([0-9a-z]+)': async (request, device, setting)
'GET /device/([0-9a-z]+)': async (request, device)
'GET /scheduler/([0-9a-z]+)': async (request, device)
'POST /disco': async (request)
'POST /security/lock': async ()
'GET /security': async ()
'PUT /security/home': async ()
'PUT /security/away': async ()
'DELETE /security': async ()
'POST /security/([Dd]isarmed|[Oo]ff|[Hh]ome|[Aa]way)': async (request, state)
'GET /test': async ()
'POST /test/(.*)': async (req, subj)
'POST /router/(.*)': async (req, subj)
'POST /warn1': async ()
'POST /warn2': async ()
'POST /warn3': async ()
'POST /alive': async () // ping from tessel
'POST /garage-error': async (request)
'GET /alarm': async ()
'PUT /alarm': async ()
'POST /alarm/stop_mv73bEuCCGxD': async ()
'POST /alarm/go': async ()
'POST /alarm/(o?[0-9]+)/([0-9]+:[0-9]+|on|off)': async (request, day, set)
'POST /button/([0-9]+)': async (request, date) // Call from AWS Lambda
'POST /oldbutton/([0-9]+)': async (request, date) // Call from AWS Lambda
'POST /cominghome': async ()
'POST /action/cominghome': async (request)
'POST /action/leaving': async (request)
'POST /action/([a-z0-9_]+)': async (request, action)
'POST /alive': async ()
'POST /opened([0-9]+)?': async (request, t) // call from tessel
'POST /closed': async () // call from tessel
'POST /close': async () // call from user
'POST /open([0-9]*)': async (request, time) // call from user
'GET /beerprogram': async ()
'GET /beer': async ()
'PUT /beer/heater': async (request)
'POST /beerprogram/([a-z0-9]+)': async (request, program)
'PUT /beerprogram': async (request)
'DELETE /beerprogram': async (request)
'DELETE /beer/heater': async (request)
'POST /beer/([^/]+)/([0-9.]+)?(/force)?': async (request, setting, temp, force)
'GET /state/cache': async ()
'DELETE /state/cache': async ()
'GET /state/history': async ()
'GET /inside': async ()
'GET /outside': async ()
'GET /weather': async ()
'GET /times': async ()
'GET /daytype/([0-9]+)': async (request, day)
'GET /daytypes/([0-9]+)-([0-9]+)': async (request, start, end)
'GET /state/thermostat': async ()
'POST /state/thermostat': async ()
'DELETE /therm/away': async ()
'PUT /therm/away': async ()
'POST /therm/temp([0-9]+)': async (request, temp)
'POST /therm/fan([0-9]+)': async (request, duration)
'POST /buttonold/([0-9]+)': async (request, date) // Call from AWS Lambda
'GET /nestredirect': async ()
'POST /nestaway': async ()
'POST /nesthome': async ()
'POST /log/([a-zA-Z]+)/([a-z0-9]+)': async (request, module, level)
 */
class Server(
  private val updateDevice: UpdateDeviceUseCase,
) {
  val server = embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
    routing {
      put("/device/{name}/{lock}") {
        updateDevice(
          name = call.pathParameters.getOrFail("name"),
          isOn = true,
          lock = call.pathParameters["lock"]?.toBoolean() ?: false
        )
      }
      delete("/device/{name}/{lock}") {
        updateDevice(
          name = call.pathParameters.getOrFail("name"),
          isOn = false,
          lock = call.pathParameters["lock"]?.toBoolean() ?: false
        )
      }
    }
  }

  fun start(){
    server.start(wait = false)
  }

  fun stop(){
    server.stop()
  }
}