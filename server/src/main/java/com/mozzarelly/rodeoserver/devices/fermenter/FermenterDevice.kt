package com.mozzarelly.rodeoserver.devices.fermenter

import com.mozzarelly.rodeoserver.devices.RodeoBluetoothDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FermenterDevice @Inject constructor(
  private val bleDevice: RodeoBluetoothDevice
) {

  fun sendCommand(command: String) {

  }

  fun getValue(name: String) {
  }
}