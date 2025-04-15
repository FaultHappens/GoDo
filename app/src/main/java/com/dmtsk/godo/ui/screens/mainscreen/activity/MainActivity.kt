package com.dmtsk.godo.ui.screens.mainscreen.activity

import android.Manifest
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dmtsk.godo.R
import com.dmtsk.godo.ui.screens.mainscreen.viewmodel.MainActivityViewModel
import com.dmtsk.godo.ui.theme.GoDoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.sign

@AndroidEntryPoint
class MainActivity : ComponentActivity()
{
	private val viewModel: MainActivityViewModel by viewModels()
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContent {
			GoDoTheme {
				GoDoMapScreen()
			}
		}
	}
	
	@OptIn(ExperimentalPermissionsApi::class, MapsComposeExperimentalApi::class)
	@Composable
	fun GoDoMapScreen()
	{
		var loading by remember { mutableStateOf(false) }
		val cameraPositionState = rememberCameraPositionState()
		var hasMovedCamera by remember { mutableStateOf(false) }
		var hasMovedCameraAfterAdventureFound by remember { mutableStateOf(false) }
		val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
		var userLocation by remember { mutableStateOf<LatLng?>(null) }
		var showFindAdventureDialog by remember { mutableStateOf(true) }
		val adventure by viewModel.adventureFlow.collectAsState()
		var adventureFound by remember { mutableStateOf(false) }
		
		val uiSettings = remember {
			MapUiSettings(
				myLocationButtonEnabled = false,
				zoomControlsEnabled = false
			)
		}
		
		// Request permission
		LaunchedEffect(Unit) {
			locationPermissionState.launchPermissionRequest()
		}
		
		LaunchedEffect(adventure) {
			adventure?.let {
				adventureFound = true
				loading = false
				Toast.makeText(this@MainActivity, "ADVENTURE FOUND", Toast.LENGTH_LONG).show()
			}
		}
		
		// Get location updates
		if (locationPermissionState.status is PermissionStatus.Granted)
		{
			val location by viewModel.locationFlow.collectAsState()
			
			LaunchedEffect(Unit) {
				viewModel.startLocationUpdates()
			}
			
			location?.let {
				val latLng = LatLng(it.latitude, it.longitude)
				userLocation = latLng
				if (!hasMovedCamera)
				{
					cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
					hasMovedCamera = true
					
				}
				Log.d("LOCATION", latLng.toString())
			}
		}
		
		Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
			
			Box(
				modifier = Modifier
					.fillMaxSize()
			) {
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.fillMaxSize()
				) {
					if (loading)
					{
						Box(
							modifier = Modifier
								.fillMaxSize()
								.background(Color.Black.copy(alpha = 0.5f)) // semi-transparent overlay
								.zIndex(1f), // keep it above other content
							contentAlignment = Alignment.Center
						) {
							CircularProgressIndicator(
								color = Color.White,
								strokeWidth = 4.dp
							)
						}
					}
					GoogleMap(
						modifier = Modifier.fillMaxSize(),
						cameraPositionState = cameraPositionState,
						properties = MapProperties(isMyLocationEnabled = locationPermissionState.status is PermissionStatus.Granted),
						uiSettings = uiSettings
					)
					{
						if (adventureFound)
						{
							val adventurePosition = LatLng(adventure!!.geometry.location.lat, adventure!!.geometry.location.lng)
							if (!hasMovedCameraAfterAdventureFound)
							{
								val bounds = LatLngBounds.builder()
									.include(adventurePosition)
									.include(userLocation!!)
									.build()
								
								val padding = 100 // pixels of padding around edges
								
								cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, padding))
								hasMovedCameraAfterAdventureFound = true
							}
							Marker(
								state = MarkerState(position = adventurePosition),
								title = adventure!!.name,
								snippet = "This is a cool place!"
							)
						}
						
						MapEffect { map ->
							//Setting map style from map_style.json
							try
							{
								val success = map.setMapStyle(
									MapStyleOptions.loadRawResourceStyle(
										this@MainActivity,
										R.raw.map_style
									)
								)
								if (!success)
								{
									Log.e("MapStyle", "Style parsing failed.")
								}
							} catch (e: Resources.NotFoundException)
							{
								Log.e("MapStyle", "Can't find style. Error: ", e)
							}
						}
					}
					if (showFindAdventureDialog)
					{
						FindAdventureDialog {
							if (userLocation != null)
							{
								loading = true
								showFindAdventureDialog = false
								findAdventure(userLocation!!)
							}
						}
					}
				}
				
				if (adventureFound)
				{
					ExpandableBottomSheet {
						repeat(50) {
							Text("Item $it", modifier = Modifier.padding(16.dp))
						}
					}
				}
			}
		}
	}
	
	@Composable
	fun ExpandableBottomSheet(
		modifier: Modifier = Modifier,
		snapPoints: List<Float> = listOf(0.1f, 0.4f, 0.95f), // as fraction of screen height
		initialSnapIndex: Int = 0,
		content: @Composable ColumnScope.() -> Unit
	)
	{
		val density = LocalDensity.current
		
		val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
		val snapPointsPx = snapPoints.map { it * screenHeightPx }
		
		var currentSnapIndex by remember { mutableStateOf(initialSnapIndex) }
		
		val animatableHeight = remember { Animatable(snapPointsPx[initialSnapIndex]) }
		var dragDeltaTotal by remember { mutableStateOf(0f) }
		
		val scope = rememberCoroutineScope()
		val dragState = rememberDraggableState { delta ->
			dragDeltaTotal += delta
			scope.launch {
				val newHeight = (animatableHeight.value - delta) // ✅ CORRECT: minus to match finger
					.coerceIn(snapPointsPx.first(), snapPointsPx.last())
				animatableHeight.snapTo(newHeight)
			}
		}
		Box(
			modifier = Modifier
				.fillMaxSize()
				.zIndex(1f),
			contentAlignment = Alignment.BottomCenter // important to anchor at bottom
		) {
			Box(
				modifier = modifier
					.fillMaxWidth()
					.height(with(density) { animatableHeight.value.toDp() })
					.background(
						color = MaterialTheme.colorScheme.surface,
						shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
					)
			) {
				Column(
					modifier = Modifier.fillMaxSize()
				) {
					// Drag handle
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(30.dp)
							.draggable(
								orientation = Orientation.Vertical,
								state = dragState,
								onDragStopped = {
									val direction = (-dragDeltaTotal).sign // Invert for correct snapping behavior
									val nextIndex = when
									{
										direction < 0 -> maxOf(0, currentSnapIndex - 1)
										direction > 0 -> minOf(snapPointsPx.lastIndex, currentSnapIndex + 1)
										else          -> currentSnapIndex
									}
									currentSnapIndex = nextIndex
									dragDeltaTotal = 0f
									
									scope.launch {
										animatableHeight.animateTo(
											targetValue = snapPointsPx[currentSnapIndex],
											animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
										)
									}
								}
							)
							.pointerInput(Unit) {
								// Needed to prevent scroll intercept from scrollable content below
							}
					) {
						// Decorative handle inside drag area
						Box(
							modifier = Modifier
								.align(Alignment.Center)
								.size(width = 80.dp, height = 4.dp)
								.background(Color.Gray, RoundedCornerShape(2.dp))
						)
					}
					
					// Scrollable content
					Column(
						modifier = Modifier
							.fillMaxSize()
							.verticalScroll(rememberScrollState())
							.padding(bottom = 32.dp)
					) {
						content()
					}
				}
			}
		}
	}
	
	private fun findAdventure(location: LatLng)
	{
		viewModel.findAdventure(location)
	}
	
	
	@Composable
	private fun FindAdventureDialog(onClick: () -> Unit)
	{
		Card(
			modifier = Modifier
				.size(width = 350.dp, height = 225.dp),
			colors = CardDefaults.cardColors(
				containerColor = Color(0xFFFFFFFF) // Light blue
			),
			shape = RoundedCornerShape(16.dp),
		) {
			Column(
				modifier = Modifier.fillMaxSize()
			) {
				TextButton(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 50.dp),
					onClick = onClick,
					shape = RoundedCornerShape(12.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.primary,
						contentColor = Color.White
					)
				) {
					Text(
						text = "Generate Adventure"
					)
				}
			}
		}
	}
}