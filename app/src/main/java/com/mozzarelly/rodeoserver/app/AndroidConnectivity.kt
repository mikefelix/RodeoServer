package com.mozzarelly.rodeoserver.app

import android.net.ConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

class AndroidConnectivity(private val connectivityManager: ConnectivityManager) : Connectivity {

  private val scope = CoroutineScope(Dispatchers.Default)

  private val listener = ConnectivityManager.OnNetworkActiveListener {
    (networkReturned as MutableSharedFlow).tryEmit(Unit)
  }

  private val flow = callbackFlow<Unit> {
    connectivityManager.addDefaultNetworkActiveListener(listener)
    awaitClose { connectivityManager.removeDefaultNetworkActiveListener(listener) }
  }.shareIn(scope, SharingStarted.Companion.Eagerly)

  override val networkReturned: SharedFlow<Unit> = MutableSharedFlow()

  override fun networkAvailable(): Boolean = connectivityManager.isDefaultNetworkActive

}