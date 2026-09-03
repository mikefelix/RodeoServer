package com.mozzarelly.rodeoserver.app

import android.R.attr.text
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mozzarelly.rodeoserver.R
import com.mozzarelly.rodeoserver.devices.Device
import com.mozzarelly.rodeoserver.devices.Subsystem
import com.mozzarelly.rodeoserver.devices.toOnText
import com.mozzarelly.rodeoserver.ui.theme.RodeoServerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RodeoServerUi()
    }

    OngoingService.startNotification(this)
  }
}

@Composable
fun RodeoServerUi(
  viewModel: MainViewModel = hiltViewModel(),
) {
  val state by viewModel.uiState.collectAsState()

  RodeoServerUi(
    status = if (state.server) "Server is up." else "Server is DOWN.",
    devices = state.devices,
    onClick = viewModel::toggleDevice
  )
}

@Composable
fun RodeoServerUi(
  status: String,
  devices: List<Device>,
  onClick: (Device) -> Unit
) {
  var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.Devices) }

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      AppDestinations.entries.forEach {
        item(
          icon = {
            Icon(
              painterResource(it.icon),
              contentDescription = it.label
            )
          },
          label = { Text(it.label) },
          selected = it == currentDestination,
          onClick = { currentDestination = it }
        )
      }
    }
  ) {
    Scaffold(modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
    ) { innerPadding ->
      MainUi(
        status = status,
        devices = devices,
        onClick = onClick,
        modifier = Modifier
          .padding(innerPadding)
      )
    }
  }
}

@Composable
fun MainUi(
  status: String,
  devices: List<Device>,
  modifier: Modifier = Modifier,
  onClick: (Device) -> Unit
){
  RodeoServerTheme {
    LazyColumn(
      verticalArrangement = spacedBy(12.dp),
      modifier = modifier
        .fillMaxSize()
    ) {
      item {
        Text(
          text = status,
          fontSize = 24.sp,
          modifier = Modifier
        )
      }

      items(devices) {
        Text(
          fontSize = 24.sp,
          text = "${it.name} (${it.subsystem}): ${it.isOn.toOnText()}",
          modifier = Modifier
            .clickable {
              onClick(it)
            }
        )
      }
    }
  }

}

enum class AppDestinations(
  val label: String,
  val icon: Int,
) {
  Devices("Devices", R.drawable.ic_home),
  Settings("Settings", R.drawable.ic_favorite),
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MainUi(
    status = "The system is down.",
    devices = listOf(Device(
      name = "office",
      subsystem = Subsystem.Tasmota,
      isOn = false,
      locked = false,
      synced = false
    )),
    onClick = {}
  )
}