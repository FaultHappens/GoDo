package com.dmtsk.godo.domain.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class GooglePlacesNearbySearchModel(
	val htmlAttributions: List<String>,
	val results: List<GooglePlaceModel>,
	val status: GooglePlacesSearchStatusEnum,
	val errorMessage: String,
	val infoMessages: List<String>,
	val nextPageToken: String
)


data class GooglePlaceModel(
	val businessStatus: GooglePlaceBusinessStatusEnum,
	val currentOpeningHours: GooglePlaceOpeningHoursModel,
	val editorialSummary: GooglePlaceEditorialSummaryModel,
	val formattedAddress: String,
	val formattedPhoneNumber: String,
	val geometry: GooglePlaceGeometryModel,
	val name: String,
	val photos: List<GooglePlacePhotoModel>,
	val placeId: String,
	val priceLevel: Int,
	val rating: Float,
	val url: String,
	val userRatingsTotal: Int
)

data class GooglePlacePhotoModel(
	val height: Int,
	val photoReference: String,
	val width: Int
)



data class GooglePlaceGeometryModel(
	val location: GooglePlaceLatLngLiteralModel // Might need to use LtLngLiteral from google
)

data class GooglePlaceLatLngLiteralModel(
	val lat: Double,
	val lng: Double
)

data class GooglePlaceEditorialSummaryModel(
	val language: String,
	val review: String
)

data class GooglePlaceOpeningHoursModel(
	val openNow: Boolean,
	val periods: List<GooglePlaceOpeningHoursPeriodModel>
)

data class GooglePlaceOpeningHoursPeriodModel(
	val open: GooglePlaceOpeningHoursPeriodDetailModel,
	val close: GooglePlaceOpeningHoursPeriodDetailModel
)

data class GooglePlaceOpeningHoursPeriodDetailModel(
	val day: Int,
	val time: Int, //0000-2359
	val date: String, // A date expressed in RFC3339 format in the local timezone for the place, for example 2010-12-31
	val truncated: Boolean
)