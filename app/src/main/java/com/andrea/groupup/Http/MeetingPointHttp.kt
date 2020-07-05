package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants
import com.andrea.groupup.Models.MeetingPoint
import com.google.gson.Gson
import org.json.JSONObject

class MeetingPointHttp (val context: Context) {
    private val URL = Constants.BASE_URL + "/meetingpoints"
    private val http = Http(context)

    fun getNow(groupId: Int, volleyCallbackArray: VolleyCallbackArray) {
        http.getAll("$URL/group/$groupId/now", volleyCallbackArray)
    }

    fun create(meetingPoint: MeetingPoint, token: String, volleyCallback: VolleyCallback) {
        http.postWithToken(URL, volleyCallback,  JSONObject(Gson().toJson(meetingPoint)), token)
    }

    fun delete(id: Int, token: String, volleyCallback: VolleyCallback) {
        http.deleteWithToken("$URL/$id", volleyCallback, token)
    }
}