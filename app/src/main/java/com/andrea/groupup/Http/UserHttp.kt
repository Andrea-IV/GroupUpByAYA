package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants

class UserHttp(val context: Context)  {
    private val URL = Constants.BASE_URL + "/users"
    private val http = Http(context)

    fun getAll(volleyCallbackArray: VolleyCallbackArray) {
        http.getAll(URL, volleyCallbackArray)
    }

    fun getByName(name: String, volleyCallbackArray: VolleyCallbackArray) {
        http.getAll("$URL?username=$name", volleyCallbackArray)
    }
}