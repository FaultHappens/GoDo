package com.dmtsk.godo.ui.screens.mainscreen.activity

import android.Manifest
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.dmtsk.godo.BuildConfig
import com.dmtsk.godo.R
import com.dmtsk.godo.domain.model.GooglePlaceModel
import com.dmtsk.godo.ui.screens.mainscreen.viewmodel.MainActivityViewModel
import com.dmtsk.godo.ui.theme.GoDoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
				Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
					GoDoMapScreen(innerPadding)
				}
			}
		}
	}
	
	@OptIn(ExperimentalPermissionsApi::class, MapsComposeExperimentalApi::class)
	@Composable
	fun GoDoMapScreen(innerPaddingValues: PaddingValues)
	{
		var loading by remember { mutableStateOf(true) }
		val cameraPositionState = rememberCameraPositionState()
		var hasMovedCamera by remember { mutableStateOf(false) }
		var hasMovedCameraAfterAdventureFound by remember { mutableStateOf(false) }
		val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
		var userLocation by remember { mutableStateOf<LatLng?>(null) }
		var showFindAdventureDialog by remember { mutableStateOf(true) }
		val adventure by viewModel.adventureFlow.collectAsState()
		var adventureFound by remember { mutableStateOf(false) }
		var fetchedUserAdventureHistory by remember { mutableStateOf(false) }
		
		val user = Firebase.auth.currentUser
		val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
		val currentDate = dateFormat.format(Date())
		val db = FirebaseFirestore.getInstance()
		val uid = Firebase.auth.currentUser?.uid
		
		val uiSettings = remember {
			MapUiSettings(
				myLocationButtonEnabled = false,
				zoomControlsEnabled = false
			)
		}
		if (!fetchedUserAdventureHistory && userLocation != null)
		{
			fetchedUserAdventureHistory = true
			fetchUserAdventureHistory(
				onSuccess = { history ->
					history.forEach { (date, placeId) ->
						if (date == currentDate)
						{
							viewModel.getAdventureInfo(placeId)
							showFindAdventureDialog = false
							return@fetchUserAdventureHistory
						}
					}
					loading = false
				},
				onFailure = { error ->
					loading = false
					Log.e("Firestore", "Failed to fetch history", error)
				}
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
				
				if (uid != null)
				{
					val updateMap = mapOf(currentDate to adventure!!.placeId)
					
					db.collection("users").document(uid)
						.set(updateMap, SetOptions.merge())
						.addOnSuccessListener {
							Log.d("Firestore", "Document successfully written!")
						}
						.addOnFailureListener { e ->
							Log.w("Firestore", "Error writing document", e)
						}
				}
				
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
			}
		}
		
		
		
		
		
		
		
		
		
		Surface(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPaddingValues), color = MaterialTheme.colorScheme.background
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
			) {
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.fillMaxSize()
				) {
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
								
								val padding = 300
								
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
					CircularImageFab(
						imageUrl = user!!.photoUrl.toString(),
						modifier = Modifier
							.align(Alignment.TopEnd)
							.padding(top = 16.dp, end = 16.dp),
						onClick = {
						
						}
					)
					if (loading)
					{
						Box(
							modifier = Modifier
								.fillMaxSize()
								.background(Color.Black.copy(alpha = 0.5f))
								.zIndex(1f),
							contentAlignment = Alignment.Center
						) {
							CircularProgressIndicator(
								color = Color.White,
								strokeWidth = 4.dp
							)
						}
					}
				}
				
				
				
				if (adventureFound && adventure != null)
				{
					ExpandableBottomSheet(adventure!!) {
						val context = LocalContext.current
						val imageLoader = ImageLoader(context)
						
						val bitmaps = remember {
							mutableStateListOf<Bitmap?>().apply {
								repeat(adventure!!.photos.size) {
									add(null)
								}
							}
						}
						
						LaunchedEffect(adventure!!.photos) {
							adventure!!.photos.forEachIndexed { index, photo ->
								launch {
									val bitmap = loadBitmapAsync(
										"https://maps.googleapis.com/maps/api/place/photo?maxwidth=300&photoreference=${
											photo
												.photoReference
										}&key=${BuildConfig.MAPS_API_KEY}", imageLoader, context
									)
									bitmaps[index] = bitmap
								}
							}
						}
						
						LazyRow(
							contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
							horizontalArrangement = Arrangement.spacedBy(12.dp)
						) {
							itemsIndexed(adventure!!.photos) { index, _ ->
								Box(
									modifier = Modifier
										.size(150.dp)
										.clip(RoundedCornerShape(12.dp)),
									contentAlignment = Alignment.Center
								) {
									val bitmap = bitmaps.getOrNull(index)
									if (bitmap != null)
									{
										Image(
											contentScale = ContentScale.Crop,
											modifier = Modifier
												.size(150.dp)
												.clip(RoundedCornerShape(12.dp)),
											bitmap = bitmap.asImageBitmap(),
											contentDescription = null,
										)
									} else
									{
										CircularProgressIndicator(modifier = Modifier.size(24.dp))
									}
								}
							}
						}
					}
				}
			}
		}
		
		
	}
	
	private fun fetchUserAdventureHistory(
		onSuccess: (Map<String, String>) -> Unit,
		onFailure: (Exception) -> Unit
	)
	{
		val db = FirebaseFirestore.getInstance()
		val uid = Firebase.auth.currentUser?.uid
		
		if (uid != null)
		{
			val userDocRef = db.collection("users").document(uid)
			
			userDocRef.get()
				.addOnSuccessListener { document ->
					if (document != null && document.exists())
					{
						// Safely cast data to Map<String, String>
						val data = document.data?.mapValues { it.value.toString() } ?: emptyMap()
						onSuccess(data)
					} else
					{
						onSuccess(emptyMap()) // No document = no history
					}
				}
				.addOnFailureListener { exception ->
					onFailure(exception)
				}
		} else
		{
			onFailure(Exception("User not authenticated"))
		}
	}
	
	
	@Composable
	fun CircularImageFab(
		imageUrl: String,
		onClick: () -> Unit,
		modifier: Modifier = Modifier
	)
	{
		FloatingActionButton(
			onClick = onClick,
			shape = CircleShape,
			containerColor = MaterialTheme.colorScheme.primary,
			modifier = modifier.size(46.dp)
		) {
			Image(
				painter = rememberAsyncImagePainter(model = imageUrl),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.size(36.dp)
					.clip(CircleShape)
			)
		}
	}
	
	@Composable
	fun ExpandableBottomSheet(
		adventure: GooglePlaceModel,
		modifier: Modifier = Modifier,
		snapPoints: List<Float> = listOf(0.1f, 0.4f, 0.95f),
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
				val newHeight = (animatableHeight.value - delta)
					.coerceIn(snapPointsPx.first(), snapPointsPx.last())
				animatableHeight.snapTo(newHeight)
			}
		}
		Box(
			modifier = Modifier
				.fillMaxSize()
				.zIndex(1f),
			contentAlignment = Alignment.BottomCenter
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
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.draggable(
								orientation = Orientation.Vertical,
								state = dragState,
								onDragStopped = {
									val direction = (-dragDeltaTotal).sign
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
							.pointerInput(Unit) {}
					) {
						Column(
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 10.dp)
						) {
							Box(
								modifier = Modifier
									.align(Alignment.CenterHorizontally)
									.padding(10.dp)
									.size(width = 80.dp, height = 4.dp)
									.background(Color.Gray, RoundedCornerShape(2.dp))
							)
							Text(
								text = stringResource(R.string.congrats_you_are_going_to, adventure.name),
								modifier = Modifier
									.align(Alignment.CenterHorizontally)
							)
						}
					}
					
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
	
	private suspend fun loadBitmapAsync(
		url: String,
		loader: ImageLoader,
		context: android.content.Context
	): Bitmap? = withContext(Dispatchers.IO) {
		try
		{
			val request = ImageRequest.Builder(context)
				.data(url)
				.allowHardware(false)
				.build()
			val result = loader.execute(request)
			if (result is SuccessResult)
			{
				return@withContext (result.drawable as android.graphics.drawable.BitmapDrawable).bitmap
			}
		} catch (_: Exception)
		{
		}
		null
	}
	
	
	@Composable
	private fun FindAdventureDialog(onClick: () -> Unit)
	{
		Card(
			modifier = Modifier
				.size(width = 350.dp, height = 225.dp),
			colors = CardDefaults.cardColors(
				containerColor = Color(0xFFFFFFFF)
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