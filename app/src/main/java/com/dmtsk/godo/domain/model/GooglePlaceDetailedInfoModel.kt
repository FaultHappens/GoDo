package com.dmtsk.godo.domain.model

data class GooglePlaceDetailedInfoModel(
	val htmlAttributions: List<String>,
	val result: GooglePlaceModel,
	val status: GooglePlacesSearchStatusEnum,
	val errorMessage: String,
	val infoMessages: List<String>,
	val nextPageToken: String
)