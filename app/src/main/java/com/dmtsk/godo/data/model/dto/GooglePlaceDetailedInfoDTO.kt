package com.dmtsk.godo.data.model.dto

import com.dmtsk.godo.domain.model.GooglePlaceDetailedInfoModel
import com.dmtsk.godo.domain.model.GooglePlacesSearchStatusEnum
import com.google.gson.annotations.SerializedName

data class GooglePlaceDetailedInfoDTO(
	
	@SerializedName("html_attributions")
	val htmlAttributions: List<String>,
	
	@SerializedName("result")
	val result: GooglePlaceDTO,
	
	@SerializedName("status")
	val status: GooglePlacesSearchStatusEnum,
	
	@SerializedName("error_message")
	val errorMessage: String?,
	
	@SerializedName("info_messages")
	val infoMessages: List<String>?,
	
	@SerializedName("next_page_token")
	val nextPageToken: String?
)

fun GooglePlaceDetailedInfoDTO.toModel(): GooglePlaceDetailedInfoModel
{
	return GooglePlaceDetailedInfoModel(
		htmlAttributions = this.htmlAttributions,
		result = this.result.toModel(),
		status = this.status,
		errorMessage = this.errorMessage ?: "",
		infoMessages = this.infoMessages ?: emptyList(),
		nextPageToken = this.nextPageToken ?: ""
	)
}
