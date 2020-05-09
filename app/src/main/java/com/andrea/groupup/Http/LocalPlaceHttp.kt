package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants

class LocalPlaceHttp (val context: Context) {
    private val URL = Constants.BASE_URL + "/localplaces"
    private val http = Http(context)

    fun getAll(volleyCallbackArray: VolleyCallbackArray) {
        http.getAll(URL, volleyCallbackArray)
    }
}