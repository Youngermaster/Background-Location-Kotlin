package com.youngermaster.backgroundlocation

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.youngermaster.backgroundlocation.ui.theme.BackgroundLocationTheme
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val  locationService: LocationService = LocationService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BackgroundLocationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LocationFetcher(locationService = locationService)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BackgroundLocationTheme {
        Greeting("Android")
    }
}

@Composable
fun LocationFetcher(locationService: LocationService) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var location by remember { mutableStateOf<Location?>(null) }
    val continuousLocation = remember { locationService.locationUpdates }.collectAsState(initial = null)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                lifecycleOwner.lifecycleScope.launch {
                    locationService.getLocationContinuous(context)
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                // Optional: Stop location updates if needed
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(onClick = {
            lifecycleOwner.lifecycleScope.launch {
                location = locationService.getLocation(context)
            }
        }) {
            Text("Get Location")
        }
        TextButton(onClick = {
            // This button is just to demonstrate triggering the start of continuous updates
            lifecycleOwner.lifecycleScope.launch {
                locationService.getLocationContinuous(context)
            }
        }) {
            Text("Start Continuous Location Updates")
        }
        continuousLocation.value?.let { loc ->
            Text("Continuous Latitude: ${loc.latitude}, Longitude: ${loc.longitude}")
        }
        location?.let { loc ->
            Text("Single Fetch Latitude: ${loc.latitude}, Longitude: ${loc.longitude}")
        }
    }
}

