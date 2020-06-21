package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants
import org.json.JSONArray
import org.json.JSONObject

class UserHttp(val context: Context)  {
    private val URL = Constants.BASE_URL + "/users"
    private val http = Http(context)

    fun getAll(volleyCallbackArray: VolleyCallbackArray) {
        http.getAll(URL, volleyCallbackArray)
    }

    fun getByName(name: String, volleyCallbackArray: VolleyCallbackArray) {
        http.getAll("$URL?username=$name", volleyCallbackArray)
    }

    fun login(username: String, password: String, volleyCallback: VolleyCallback) {
        val body = JSONObject("{\"username\": \"$username\", \"password\": \"$password\"}")
        http.post("$URL/login", volleyCallback, body)
    }
}