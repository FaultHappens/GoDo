package com.dmtsk.godo.data.model.request

data class GooglePlacesNearbySearchRequest(
	var location: String,
	var radius: Int,
	var type: String
)

fun GooglePlacesNearbySearchRequest.toQueryMap(): Map<String, String> {
	return mapOf(
		"location" to location,
		"radius" to radius.toString(),
		"type" to type
	)
}