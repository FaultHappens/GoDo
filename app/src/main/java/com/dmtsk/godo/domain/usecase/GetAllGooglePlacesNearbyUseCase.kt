package com.dmtsk.godo.domain.usecase

import com.dmtsk.godo.data.model.request.GooglePlacesNearbySearchRequest
import com.dmtsk.godo.domain.model.GooglePlacesNearbySearchModel
import com.dmtsk.godo.domain.repository.GoogleMapsRepository
import javax.inject.Inject

class GetAllGooglePlacesNearbyUseCase @Inject constructor(
	private val googleMapsRepository: GoogleMapsRepository
)
{
	suspend operator fun invoke(googlePlacesNearbySearchRequest: GooglePlacesNearbySearchRequest): GooglePlacesNearbySearchModel {
		return googleMapsRepository.getAllGooglePlacesNearbyByType(googlePlacesNearbySearchRequest)
	}
}