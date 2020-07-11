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

    fun getByGroup(groupId: Int, lat: Double?, lng: Double?, token: String, volleyCallbackArray: VolleyCallbackArray) {
        http.getAllWithToken("$URL/group/$groupId?lat=$lat&lng=$lng", token, volleyCallbackArray)
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

    fun getByLatLngAndTrad(lat: String, lng: String, lang: String, volleyCallbackArray: VolleyCallbackArray) {
        http.getAll("$URL/lat/$lat/lng/$lng/trad/$lang", volleyCallbackArray)
    }

    fun createPlace(params: String, token: String, volleyCallback: VolleyCallback){
        http.postWithToken(URL, volleyCallback, JSONObject(params), token)
    }

    fun deletePlace(localPlaceId: String, volleyCallback: VolleyCallback){
        http.delete("$URL/$localPlaceId", volleyCallback)
    }

    fun addTags(tags: ArrayList<Tag>, idLocalPlace: String, volleyCallback: VolleyCallback){
        var params = "{\"tags\":["
        for(tag: Tag in tags){
            params += "\"${tag.name}\","
        }
        params = params.substring(0, params.lastIndex) + "]}"
        http.post("$URL/$idLocalPlace/addTags", volleyCallback, JSONObject(params))
    }

    fun addTag(tags: ArrayList<Tag>, newTag: String, idLocalPlace: String, volleyCallback: VolleyCallback){
        var params: String = "{\"tags\":["
        for(tag: Tag in tags){
            params += "\"${tag.name}\","
        }
        params += "$newTag]}"
        http.post("$URL/$idLocalPlace/addTags", volleyCallback, JSONObject(params))
    }

    fun deleteTag(tags: ArrayList<Tag>, idLocalPlace: String, volleyCallback: VolleyCallback){
        var params: String
        if(tags.isNotEmpty()){
            params = "{\"tags\":["
            for(tag: Tag in tags){
                params += "\"${tag.name}\","
            }
            params = params.substring(0, params.lastIndex) + "]}"
        }else{
            params = "{\"tags\":[]}"
        }

        http.post("$URL/$idLocalPlace/addTags", volleyCallback, JSONObject(params))
    }
}