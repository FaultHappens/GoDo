package com.dmtsk.godo.data.model.dto

import com.dmtsk.godo.domain.model.GooglePlaceBusinessStatusEnum
import com.dmtsk.godo.domain.model.GooglePlaceEditorialSummaryModel
import com.dmtsk.godo.domain.model.GooglePlaceGeometryModel
import com.dmtsk.godo.domain.model.GooglePlaceLatLngLiteralModel
import com.dmtsk.godo.domain.model.GooglePlaceModel
import com.dmtsk.godo.domain.model.GooglePlaceOpeningHoursModel
import com.dmtsk.godo.domain.model.GooglePlaceOpeningHoursPeriodDetailModel
import com.dmtsk.godo.domain.model.GooglePlaceOpeningHoursPeriodModel
import com.dmtsk.godo.domain.model.GooglePlacePhotoModel
import com.dmtsk.godo.domain.model.GooglePlacesNearbySearchModel
import com.dmtsk.godo.domain.model.GooglePlacesSearchStatusEnum
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class GooglePlacesNearbySearchDTO(
	@SerializedName("html_attributions")
	val htmlAttributions: List<String>,
	@SerializedName("results")
	val results: List<GooglePlaceDTO>,
	@SerializedName("status")
	val status: GooglePlacesSearchStatusEnum,
	@SerializedName("error_message")
	val errorMessage: String?,
	@SerializedName("info_messages")
	val infoMessages: List<String>?,
	@SerializedName("next_page_token")
	val nextPageToken: String?
)

fun GooglePlacesNearbySearchDTO.toModel(): GooglePlacesNearbySearchModel
{
	return GooglePlacesNearbySearchModel(
		htmlAttributions = this.htmlAttributions,
		results = this.results.map { it.toModel() },
		status = this.status,
		errorMessage = this.errorMessage ?: "",
		infoMessages = this.infoMessages ?: emptyList(),
		nextPageToken = this.nextPageToken ?: ""
	)
}


data class GooglePlaceDTO(
	@SerializedName("business_status")
	val businessStatus: GooglePlaceBusinessStatusEnum?,
	@SerializedName("current_opening_hours")
	val currentOpeningHours: GooglePlaceOpeningHoursDTO?,
	@SerializedName("editorial_summary")
	val editorialSummary: GooglePlaceEditorialSummaryDTO?,
	@SerializedName("formatted_address")
	val formattedAddress: String?,
	@SerializedName("formatted_phone_number")
	val formattedPhoneNumber: String?,
	@SerializedName("geometry")
	val geometry: GooglePlaceGeometryDTO?,
	@SerializedName("name")
	val name: String?,
	@SerializedName("photos")
	val photos: List<GooglePlacePhotoDTO>?,
	@SerializedName("place_id")
	val placeId: String?,
	@SerializedName("price_level")
	val priceLevel: Int?,
	@SerializedName("rating")
	val rating: Float?,
	@SerializedName("url")
	val url: String?,
	@SerializedName("user_ratings_total")
	val userRatingsTotal: Int?
)

fun GooglePlaceDTO.toModel(): GooglePlaceModel
{
	return GooglePlaceModel(
		businessStatus = this.businessStatus ?: GooglePlaceBusinessStatusEnum.UNKNOWN,
		currentOpeningHours = this.currentOpeningHours.toModel(),
		editorialSummary = this.editorialSummary.toModel(),
		formattedAddress = this.formattedAddress ?: "",
		formattedPhoneNumber = this.formattedPhoneNumber ?: "",
		geometry = this.geometry.toModel(),
		name = this.name ?: "",
		photos = if (this.photos != null) this.photos.map { it.toModel() } else emptyList<GooglePlacePhotoModel>(), //BE CAREFUL change this
		placeId = this.placeId ?: "",
		priceLevel = this.priceLevel ?: -1,
		rating = this.rating ?: -1F,
		url = this.url ?: "",
		userRatingsTotal = this.userRatingsTotal ?: -1
	)
}

data class GooglePlacePhotoDTO(
	@SerializedName("height")
	val height: Int,
	@SerializedName("photo_reference")
	val photoReference: String,
	@SerializedName("width")
	val width: Int
)

fun GooglePlacePhotoDTO.toModel(): GooglePlacePhotoModel
{
	return GooglePlacePhotoModel(
		height = this.height,
		photoReference = this.photoReference,
		width = this.width
	)
}

data class GooglePlaceGeometryDTO(
	@SerializedName("location")
	val location: GooglePlaceLatLngLiteralDTO // Might need to use LtLngLiteral from google
)

fun GooglePlaceGeometryDTO?.toModel(): GooglePlaceGeometryModel
{
	return if (this != null)
	{
		GooglePlaceGeometryModel(
			location = this.location.toModel()
		)
	} else
	{
		GooglePlaceGeometryModel(
			location = GooglePlaceLatLngLiteralModel(0.0, 0.0)
		)
	}
}

data class GooglePlaceLatLngLiteralDTO(
	@SerializedName("lat")
	val lat: Double,
	@SerializedName("lng")
	val lng: Double
)

fun GooglePlaceLatLngLiteralDTO.toModel(): GooglePlaceLatLngLiteralModel
{
	return GooglePlaceLatLngLiteralModel(
		lat = this.lat,
		lng = this.lng
	)
}


data class GooglePlaceEditorialSummaryDTO(
	@SerializedName("language")
	val language: String?,
	@SerializedName("review")
	val review: String?
)

fun GooglePlaceEditorialSummaryDTO?.toModel(): GooglePlaceEditorialSummaryModel
{
	return if (this != null)
	{
		GooglePlaceEditorialSummaryModel(
			language = this.language ?: "",
			review = this.review ?: ""
		)
	} else
	{
		GooglePlaceEditorialSummaryModel(
			language = "",
			review = ""
		)
	}
}

data class GooglePlaceOpeningHoursDTO(
	@SerializedName("open_now")
	val openNow: Boolean,
	@SerializedName("periods")
	val periods: List<GooglePlaceOpeningHoursPeriodDTO>
)

fun GooglePlaceOpeningHoursDTO?.toModel(): GooglePlaceOpeningHoursModel
{
	return if (this != null)
	{
		GooglePlaceOpeningHoursModel(
			openNow = this.openNow,
			periods = this.periods.map { it.toModel() }
		)
	} else
	{
		GooglePlaceOpeningHoursModel(
			openNow = false,
			periods = emptyList()
		)
	}
}

data class GooglePlaceOpeningHoursPeriodDTO(
	@SerializedName("open")
	val open: GooglePlaceOpeningHoursPeriodDetailDTO,
	@SerializedName("close")
	val close: GooglePlaceOpeningHoursPeriodDetailDTO
)

fun GooglePlaceOpeningHoursPeriodDTO.toModel(): GooglePlaceOpeningHoursPeriodModel
{
	return GooglePlaceOpeningHoursPeriodModel(
		open = this.open.toModel(),
		close = this.close.toModel()
	)
}

data class GooglePlaceOpeningHoursPeriodDetailDTO(
	@SerializedName("day")
	val day: Int,
	@SerializedName("time")
	val time: Int, //0000-2359
	@SerializedName("date")
	val date: String, // A date expressed in RFC3339 format in the local timezone for the place, for example 2010-12-31
	@SerializedName("truncated")
	val truncated: Boolean
)

fun GooglePlaceOpeningHoursPeriodDetailDTO.toModel(): GooglePlaceOpeningHoursPeriodDetailModel
{
	return GooglePlaceOpeningHoursPeriodDetailModel(
		day = this.day,
		time = this.time,
		date = this.date,
		truncated = this.truncated
	)
}

