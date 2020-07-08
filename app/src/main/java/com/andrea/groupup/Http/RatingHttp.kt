package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants
import org.json.JSONObject

class RatingHttp(val context: Context) {
    private val URL = Constants.BASE_URL + "/ratings"
    private val http = Http(context)

    fun createRating(rating: String, idLocalPlace: String, token: String, volleyCallback: VolleyCallback){
        http.postWithToken(URL, volleyCallback, JSONObject("{\"rating\":$rating,\"localPlaceId\":$idLocalPlace}"), token)
    }

    fun modifyRating(rating: String, idLocalPlace: String, token: String, volleyCallback: VolleyCallback){
        http.putWithTokenAndParams(URL, volleyCallback, JSONObject("{\"rating\":$rating,\"localPlaceId\":$idLocalPlace}"), token)
    }

    fun deleteRating(idLocalPlace: String, token: String, volleyCallback: VolleyCallback){
        http.deleteWithToken("$URL/$idLocalPlace", volleyCallback, token)
    }
}