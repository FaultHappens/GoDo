package com.dmtsk.godo.data.network.apiservice

import com.dmtsk.godo.data.model.dto.GooglePlaceDetailedInfoDTO
import com.dmtsk.godo.data.model.dto.GooglePlacesNearbySearchDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface GoogleMapsApiService
{
	@GET("place/nearbysearch/json")
	suspend fun getAllGooglePlacesNearbyByType(@QueryMap params: Map<String, String>): Response<GooglePlacesNearbySearchDTO>
	
	@GET("place/nearbysearch/json")
	suspend fun getGooglePlacesNearbyNextPage(@Query("pagetoken") pageToken: String): Response<GooglePlacesNearbySearchDTO>
	
	@GET("place/details/json")
	suspend fun getGooglePlaceDetailedInfo(@Query("place_id") placeID: String): Response<GooglePlaceDetailedInfoDTO>
	
	
}