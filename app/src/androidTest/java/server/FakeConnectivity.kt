package server

import com.mozzarelly.rodeoserver.app.Connectivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class FakeConnectivity : Connectivity {
  private val connectivityFlow = MutableSharedFlow<Unit>()
  var networkAvailable = true
    set(value) {
      field = value
      if (value) {
        connectivityFlow.tryEmit(Unit)
      }
    }
  override val networkReturned: SharedFlow<Unit> = connectivityFlow
  override fun networkAvailable(): Boolean = networkAvailable
}