package com.example.rodeoserver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.rodeoserver.ui.theme.RodeoServerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RodeoServerTheme {
        RodeoServerApp("Server is up.")
      }
    }
  }
}

@Composable
fun RodeoServerApp(
  status: String,
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
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      Greeting(
        text = status,
        modifier = Modifier.padding(innerPadding)
      )
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

@Composable
fun Greeting(
  text: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = "Hello $text!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  RodeoServerTheme {
    Greeting("Server status: UP!")
  }
}