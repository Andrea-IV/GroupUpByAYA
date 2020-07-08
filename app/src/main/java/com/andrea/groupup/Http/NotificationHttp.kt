package com.andrea.groupup.Http

import android.content.Context
import android.util.Log
import com.andrea.groupup.Constants
import com.andrea.groupup.Models.MeetingPoint
import com.andrea.groupup.Models.Notification
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONObject

class NotificationHttp (val context: Context) {
    private val URL = Constants.BASE_URL + "/notifications"
    private val http = Http(context)

    fun send(notification: Notification, token: String, volleyCallback: VolleyCallback) {
        Log.d("HTTP", "NotificationHttp - send")
        http.postWithToken(URL, volleyCallback,  JSONObject(Gson().toJson(notification)), token)
        println(Gson().toJson(notification))
    }
}