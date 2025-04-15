package com.dmtsk.godo.domain.usecase

import com.dmtsk.godo.data.model.request.GooglePlacesNearbyNextPageRequest
import com.dmtsk.godo.domain.model.GooglePlacesNearbySearchModel
import com.dmtsk.godo.domain.repository.GoogleMapsRepository
import javax.inject.Inject

class GetGooglePlacesNearbyNextPageUseCase @Inject constructor(
	private val googleMapsRepository: GoogleMapsRepository
)
{
	suspend operator fun invoke(request: GooglePlacesNearbyNextPageRequest): GooglePlacesNearbySearchModel
	{
		return googleMapsRepository.getGooglePlacesNearbyNextPage(request)
	}
}