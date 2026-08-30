package com.mozzarelly.rodeoserver.devices

interface RodeoBluetoothDevice {
  fun onDataReceived(data: ByteArray)
}