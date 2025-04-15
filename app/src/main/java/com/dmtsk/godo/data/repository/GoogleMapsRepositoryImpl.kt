package com.dmtsk.godo.data.repository

import com.dmtsk.godo.data.model.dto.toModel
import com.dmtsk.godo.data.model.request.GooglePlaceDetailedInfoRequest
import com.dmtsk.godo.data.model.request.GooglePlacesNearbyNextPageRequest
import com.dmtsk.godo.data.model.request.GooglePlacesNearbySearchRequest
import com.dmtsk.godo.data.model.request.toQueryMap
import com.dmtsk.godo.data.network.apiservice.GoogleMapsApiService
import com.dmtsk.godo.domain.model.GooglePlaceDetailedInfoModel
import com.dmtsk.godo.domain.model.GooglePlaceModel
import com.dmtsk.godo.domain.model.GooglePlacesNearbySearchModel
import com.dmtsk.godo.domain.repository.GoogleMapsRepository

class GoogleMapsRepositoryImpl(private val googleMapsApiService: GoogleMapsApiService) : GoogleMapsRepository
{
	override suspend fun getAllGooglePlacesNearbyByType(request: GooglePlacesNearbySearchRequest): GooglePlacesNearbySearchModel
	{
		val response = googleMapsApiService.getAllGooglePlacesNearbyByType(request.toQueryMap())
		if (response.isSuccessful)
		{
			response.body()?.let { dataDto ->
				return dataDto.toModel()
			}
		}
		
		throw Exception("Failed to fetch data: ${response.code()} - ${response.message()}")
	}
	
	override suspend fun getGooglePlacesNearbyNextPage(request: GooglePlacesNearbyNextPageRequest): GooglePlacesNearbySearchModel
	{
		val response = googleMapsApiService.getGooglePlacesNearbyNextPage(request.pageToken)
		if (response.isSuccessful)
		{
			response.body()?.let { dataDto ->
				return dataDto.toModel()
			}
		}
		
		throw Exception("Failed to fetch data: ${response.code()} - ${response.message()}")
	}
	
	override suspend fun getGooglePlaceDetailedInfo(request: GooglePlaceDetailedInfoRequest): GooglePlaceDetailedInfoModel
	{
		val response = googleMapsApiService.getGooglePlaceDetailedInfo(request.placeId)
		if (response.isSuccessful)
		{
			response.body()?.let { dataDto ->
				return dataDto.toModel()
			}
		}
		
		throw Exception("Failed to fetch data: ${response.code()} - ${response.message()}")
	}
}