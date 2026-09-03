package com.mozzarelly.rodeoserver.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mozzarelly.rodeoserver.devices.Device
import com.mozzarelly.rodeoserver.devices.GetDeviceStateUseCase
import com.mozzarelly.rodeoserver.devices.ToggleDeviceUseCase
import com.mozzarelly.rodeoserver.devices.UpdateDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
  val server: Boolean,
  val devices: List<Device>,
)

@HiltViewModel
class MainViewModel @Inject constructor(
  private val toggleDeviceUseCase: ToggleDeviceUseCase,
  private val getDeviceStateUseCase: GetDeviceStateUseCase
) : ViewModel() {

  val uiState: StateFlow<MainUiState> = getDeviceStateUseCase()
    .map { MainUiState(
      true,
      it
    ) }
    .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), MainUiState(false, emptyList()))

  fun toggleDevice(device: Device) {
    viewModelScope.launch {
      toggleDeviceUseCase(device)
    }
  }
}