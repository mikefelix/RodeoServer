package com.mozzarelly.rodeoserver.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mozzarelly.rodeoserver.devices.DeviceRepository
import com.mozzarelly.rodeoserver.devices.GetDeviceStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MainUiState(
  val server: Boolean,
  val devices: List<String>,
)

@HiltViewModel
class MainViewModel @Inject constructor(
  val deviceRepository: DeviceRepository
) : ViewModel() {

  private val deviceStateUseCase = GetDeviceStateUseCase(deviceRepository)

  val uiState: StateFlow<MainUiState> = deviceStateUseCase()
    .map { MainUiState(
      true,
      it.map { it.name + ": " + if (it.isOn) "on" else "off" }
    ) }
    .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), MainUiState(false, emptyList()))

}