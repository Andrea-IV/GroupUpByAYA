package com.andrea.groupup.Http

import android.content.Context
import android.util.Log
import com.andrea.groupup.Constants
import com.andrea.groupup.Models.LocalPlace
import com.andrea.groupup.Models.Tag
import org.json.JSONObject

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

    fun createPlace(place: LocalPlace, token: String, volleyCallback: VolleyCallback){
        val params = "{\"name\":\"${place.name}\", \"coordinate_x\":\"${place.coordinate_x}\", \"coordinate_y\":\"${place.coordinate_y}\", \"address\":\"${place.address}\", \"opening_hour\":\"${place.opening_hour}\", \"closing_hour\":\"${place.closing_hour}\"}"
        http.postWithToken(URL, volleyCallback, JSONObject(params), token)
    }

    fun addTag(tags: ArrayList<Tag>, newTag: String, idLocalPlace: String, volleyCallback: VolleyCallback){
        var params: String = "{\"tags\":["
        for(tag: Tag in tags){
            params += "\"${tag.name}\","
        }
        params += "$newTag]}"
        http.post("$URL/$idLocalPlace/addTags", volleyCallback, JSONObject(params))
    }
}