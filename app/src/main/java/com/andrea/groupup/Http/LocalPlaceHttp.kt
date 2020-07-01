package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants

class LocalPlaceHttp (val context: Context) {
    private val URL = Constants.BASE_URL + "/localplaces"
    private val http = Http(context)

    fun getAll(volleyCallbackArray: VolleyCallbackArray) {
        http.getAll("$URL/", volleyCallbackArray)
    }

    fun getOne(id:String, volleyCallback: VolleyCallback) {
        http.getOne("$URL/$id", volleyCallback)
    }

    fun getAllWithTrad(language: String, token: String, volleyCallbackArray: VolleyCallbackArray) {
        http.getAllWithToken("$URL/trad/$language", token, volleyCallbackArray)
    }

    fun getAllWithTradAndDist(language: String, posx: String, posy: String, token: String, volleyCallbackArray: VolleyCallbackArray) {
        http.getAllWithToken("$URL/trad/$language?pos_x=$posx&pos_y=$posy", token, volleyCallbackArray)
    }

    fun getByLatLng(lat: String, lng: String, volleyCallbackArray: VolleyCallbackArray) {
        http.getAll("$URL/lat/$lat/lng/$lng", volleyCallbackArray)
    }
}