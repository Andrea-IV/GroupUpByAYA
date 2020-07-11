package com.andrea.groupup.Http

import android.content.Context
import android.util.EventLog
import android.util.Log
import com.andrea.groupup.Constants
import com.andrea.groupup.Models.Event
import com.andrea.groupup.Models.LocalPlace
import org.json.JSONObject

class EventHttp(val context: Context)  {
    private val URL = Constants.BASE_URL + "/travels"
    private val http = Http(context)

    fun getEvents(id: String, token: String, volleyCallbackArray: VolleyCallbackArray){
        http.getAllWithToken("$URL/group/$id", token, volleyCallbackArray)
    }

    fun createEvents(date: String, groupId: String, placeId: String, token: String, volleyCallback: VolleyCallback){
        val params = "{\"id\":\"$date\",\"travel_date\":\"$date\",\"group_id\":$groupId,\"localplaces\":[{\"id\":$placeId,\"position\":1}]}"
        http.postWithToken("$URL/", volleyCallback, JSONObject(params), token)
    }

    fun modifyEvents(event: Event, newPlaceId: String, token: String, volleyCallback: VolleyCallback){
        var params = "{\"id\":\"${event.id}\",\"travel_date\":\"${event.travel_date_original}\",\"localplaces\":["
        for(localPlace in event.LocalPlaces){
            params += "{\"id\":${localPlace.id},\"position\":${localPlace.pos}},"
        }
        params += "{\"id\":${newPlaceId},\"position\":${event.LocalPlaces.size + 1}}]}"
        http.putWithTokenAndParams("$URL/", volleyCallback, JSONObject(params), token)
    }

    fun deletePlan(event: Event, token: String, volleyCallback: VolleyCallback){
        var params = "{\"id\":\"${event.id}\",\"travel_date\":\"${event.travel_date_original}\",\"localplaces\":["
        for(localPlace in event.LocalPlaces){
            params += "{\"id\":${localPlace.id},\"position\":${localPlace.pos}},"
        }
        params = params.substring(0, params.lastIndex) + "]}"
        http.putWithTokenAndParams("$URL/", volleyCallback, JSONObject(params), token)
    }

    fun deleteEvent(event: Event, token: String, volleyCallback: VolleyCallback){
        http.deleteWithToken("$URL/${event.id}", volleyCallback, token)
    }
}