package com.mozzarelly.rodeoserver.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mozzarelly.rodeoserver.R
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
  }
}

@Composable
fun RodeoServerUi(
  viewModel: MainViewModel = hiltViewModel(),
) {
  val state by viewModel.uiState.collectAsState()

  RodeoServerUi(
    status = state.server.toString(),
    devices = state.devices
  )
}

@Composable
fun RodeoServerUi(
  status: String,
  devices: List<String>,
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
        listOf(status) + devices,
        modifier = Modifier
          .padding(innerPadding)
      )
    }
  }
}

@Composable
fun MainUi(
  items: List<String>,
  modifier: Modifier = Modifier
){
  RodeoServerTheme {
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
    ) {
      items(items) {
        Text(
          text = it,
          modifier = Modifier
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
  MainUi(listOf("The system is down.", "Devices:", "bed: off", "office: on"))
}