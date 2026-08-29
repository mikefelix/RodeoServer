package com.mozzarelly.rodeoserver.app

import android.bluetooth.BluetoothDevice
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.util.regex.Pattern

class BluetoothPairActivity : ComponentActivity() {
  val deviceFilter = BluetoothDeviceFilter.Builder()
    .setNamePattern(Pattern.compile("MyDevice.*"))
    // or .addServiceUuid(ParcelUuid(MY_SERVICE_UUID), null)
    .build()

  val launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
    val device: BluetoothDevice? = result.data
      ?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
  }

  fun onPair() {
    val pairingRequest = AssociationRequest.Builder()
      .addDeviceFilter(deviceFilter)
      .setSingleDevice(false) // true = auto-pick if exactly one match
      .build()

    val deviceManager = getSystemService(CompanionDeviceManager::class.java)

    deviceManager.associate(pairingRequest, object : CompanionDeviceManager.Callback() {
      override fun onAssociationPending(intentSender: IntentSender) {
        // launch this to show system chooser UI
        startIntentSenderForResult(intentSender, 56488, null, 0, 0, 0)
      }

      override fun onAssociationCreated(associationInfo: AssociationInfo) {
        // API 33+, gives you AssociationInfo directly
      }

      override fun onFailure(error: CharSequence?) {}
    }, null)
  }
}