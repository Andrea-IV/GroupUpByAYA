package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants

class TravelHttp (val context: Context)  {

    private val URL = Constants.BASE_URL + "/travels"
    private val http = Http(context)

    fun getTodaysTravel(group_id: Number, token: String, volleyCallback: VolleyCallback) {
        http.getOneWithToken("$URL/group/$group_id/today", token, volleyCallback)
    }
}