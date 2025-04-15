package com.dmtsk.godo.domain.usecase

import com.dmtsk.godo.data.model.request.GooglePlaceDetailedInfoRequest
import com.dmtsk.godo.data.model.request.GooglePlacesNearbySearchRequest
import com.dmtsk.godo.domain.model.GooglePlaceDetailedInfoModel
import com.dmtsk.godo.domain.model.GooglePlaceModel
import com.dmtsk.godo.domain.model.GooglePlacesNearbySearchModel
import com.dmtsk.godo.domain.repository.GoogleMapsRepository
import javax.inject.Inject

class GetGooglePlaceDetailedInfoUseCase @Inject constructor(
	private val googleMapsRepository: GoogleMapsRepository
)
{
	suspend operator fun invoke(request: GooglePlaceDetailedInfoRequest): GooglePlaceDetailedInfoModel
	{
		return googleMapsRepository.getGooglePlaceDetailedInfo(request)
	}
}