package com.dmtsk.godo.domain.repository

import com.dmtsk.godo.data.model.request.GooglePlaceDetailedInfoRequest
import com.dmtsk.godo.data.model.request.GooglePlacesNearbyNextPageRequest
import com.dmtsk.godo.data.model.request.GooglePlacesNearbySearchRequest
import com.dmtsk.godo.domain.model.GooglePlaceDetailedInfoModel
import com.dmtsk.godo.domain.model.GooglePlaceModel
import com.dmtsk.godo.domain.model.GooglePlacesNearbySearchModel

interface GoogleMapsRepository
{
	suspend fun getAllGooglePlacesNearbyByType(request: GooglePlacesNearbySearchRequest): GooglePlacesNearbySearchModel
	suspend fun getGooglePlacesNearbyNextPage(request: GooglePlacesNearbyNextPageRequest): GooglePlacesNearbySearchModel
	suspend fun getGooglePlaceDetailedInfo(request: GooglePlaceDetailedInfoRequest): GooglePlaceDetailedInfoModel
}