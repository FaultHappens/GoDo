package com.dmtsk.godo.ui.screens.mainscreen.viewmodel

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmtsk.godo.data.model.request.GooglePlaceDetailedInfoRequest
import com.dmtsk.godo.data.model.request.GooglePlacesNearbyNextPageRequest
import com.dmtsk.godo.data.model.request.GooglePlacesNearbySearchRequest
import com.dmtsk.godo.domain.model.GooglePlaceModel
import com.dmtsk.godo.domain.usecase.GetAllGooglePlacesNearbyUseCase
import com.dmtsk.godo.domain.usecase.GetGooglePlaceDetailedInfoUseCase
import com.dmtsk.godo.domain.usecase.GetGooglePlacesNearbyNextPageUseCase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random


@HiltViewModel
class MainActivityViewModel @Inject constructor(
	private val fusedLocationClient: FusedLocationProviderClient,
	private val getAllGooglePlacesNearbyUseCase: GetAllGooglePlacesNearbyUseCase,
	private val getGooglePlacesNearbyNextPageUseCase: GetGooglePlacesNearbyNextPageUseCase,
	private val getGooglePlaceDetailedInfoUseCase: GetGooglePlaceDetailedInfoUseCase,
	@ApplicationContext private val context: Context
) : ViewModel()
{
	private val _locationFlow = MutableStateFlow<Location?>(null)
	val locationFlow: StateFlow<Location?> = _locationFlow.asStateFlow()
	
	private val _adventureFlow = MutableStateFlow<GooglePlaceModel?>(null)
	val adventureFlow: StateFlow<GooglePlaceModel?> = _adventureFlow.asStateFlow()
	
	private val locationRequest = LocationRequest.Builder(
		Priority.PRIORITY_HIGH_ACCURACY,
		200L // 200ms
	).apply {
		setMinUpdateIntervalMillis(200)
	}.build()
	
	private val locationCallback = object : LocationCallback()
	{
		override fun onLocationResult(result: LocationResult)
		{
			result.lastLocation?.let {
				_locationFlow.value = it
			}
		}
	}
	
	@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
	fun startLocationUpdates()
	{
		fusedLocationClient.requestLocationUpdates(
			locationRequest,
			locationCallback,
			Looper.getMainLooper()
		)
	}
	
	fun stopLocationUpdates()
	{
		fusedLocationClient.removeLocationUpdates(locationCallback)
	}
	
	override fun onCleared()
	{
		super.onCleared()
		stopLocationUpdates()
	}
	
	fun findAdventure(location: LatLng)
	{
		val type = getRandomType()
		viewModelScope.launch {
			val places = getAllNearByPlaces(location, type)
			val randomPlace = places.random()
			val randomPlaceAdditionalInfo = getGooglePlaceDetailedInfoUseCase.invoke(GooglePlaceDetailedInfoRequest(randomPlace.placeId))
			_adventureFlow.value = randomPlaceAdditionalInfo.result
		}
		
		
	}
	
	private fun getRandomType(): String
	{
		return "restaurant"
	}
	
	private suspend fun getAllNearByPlaces(location: LatLng, type: String): List<GooglePlaceModel>
	{
		//First Page
		val returnList = mutableListOf<GooglePlaceModel>()
		var response = getAllGooglePlacesNearbyUseCase.invoke(
			googlePlacesNearbySearchRequest = GooglePlacesNearbySearchRequest(
				location = "${location.latitude},${location.longitude}",
				radius = 1500,
				type = type
			)
		)
		Log.d("RESPONSE", response.toString())
		returnList.addAll(response.results)
		
		while(response.nextPageToken.isNotEmpty()){
			delay(2000)
			response = getGooglePlacesNearbyNextPageUseCase.invoke(GooglePlacesNearbyNextPageRequest(response.nextPageToken))
			returnList.addAll(response.results)
			Log.d("NEXT PAGE", response.toString())
		}
		
		return returnList
	}
}