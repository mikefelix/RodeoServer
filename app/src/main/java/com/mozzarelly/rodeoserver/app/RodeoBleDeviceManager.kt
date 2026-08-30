package com.mozzarelly.rodeoserver.app

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.mozzarelly.rodeoserver.devices.RodeoBluetoothDevice
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.BleManagerCallbacks
import no.nordicsemi.android.ble.data.Data
import java.util.UUID

val MY_SERVICE_UUID: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
val MY_CHAR_UUID: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")

abstract class RodeoBleDeviceManager(context: Context) :
  BleManager(context), BleManagerCallbacks, RodeoBluetoothDevice {
  private var myCharacteristic: BluetoothGattCharacteristic? = null

  init {
    setGattCallbacks(this)
  }

  private val gattCallback by lazy {
    object: BleManagerGattCallback() {
      override fun onDeviceDisconnected() {}
      override fun onServicesInvalidated() {}

      override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(MY_SERVICE_UUID)
        myCharacteristic = service?.getCharacteristic(MY_CHAR_UUID)
        return myCharacteristic != null
      }

      override fun initialize() {
        setNotificationCallback(myCharacteristic).with { _: BluetoothDevice, data: Data ->
          data.value?.let { onDataReceived(it) }
        }

        enableNotifications(myCharacteristic).enqueue()
      }
    }
  }

  override fun getGattCallback(): BleManagerGattCallback = gattCallback

  override fun onDeviceConnecting(p0: BluetoothDevice) {}
  override fun onDeviceConnected(p0: BluetoothDevice) {}
  override fun onDeviceDisconnecting(p0: BluetoothDevice) {}
  override fun onDeviceDisconnected(p0: BluetoothDevice) {}
  override fun onLinkLossOccurred(p0: BluetoothDevice) {}
  override fun onServicesDiscovered(p0: BluetoothDevice, p1: Boolean) {}
  override fun onDeviceReady(p0: BluetoothDevice) {}
  override fun onBondingRequired(p0: BluetoothDevice) {}
  override fun onBonded(p0: BluetoothDevice) {}
  override fun onBondingFailed(p0: BluetoothDevice) {}
  override fun onError(p0: BluetoothDevice, p1: String, p2: Int) {}
  override fun onDeviceNotSupported(p0: BluetoothDevice) {}
}