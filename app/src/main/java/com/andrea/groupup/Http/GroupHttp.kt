package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants
import com.andrea.groupup.Http.Http
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Http.VolleyCallbackArray
import org.json.JSONObject

class GroupHttp(val context: Context)  {
    private val URL = Constants.BASE_URL + "/groups"
    private val http = Http(context)

    fun createGroup(name: String, token: String, volleyCallback: VolleyCallback){
        http.postWithToken(URL, volleyCallback, JSONObject("{\"name\": \"$name\"}"), token)
    }

    fun getGroupForUser(id: String, volleyCallbackArray: VolleyCallbackArray) {
        http.getAll("$URL/user/$id", volleyCallbackArray)
    }

    fun addToGroup(idGroup: String, idUser: String, token: String, volleyCallback: VolleyCallback){
        http.postWithToken("$URL/$idGroup/user/$idUser", volleyCallback, JSONObject("{}"), token)
    }

    fun leaveGroup(idGroup: String, token: String, volleyCallback: VolleyCallback){
        http.deleteWithToken("$URL/$idGroup/leave/", volleyCallback, token)
    }

    fun kickGroup(idGroup: String, idUser: String, token: String, volleyCallback: VolleyCallback){
        http.deleteWithToken("$URL/$idGroup/kick/$idUser", volleyCallback, token)
    }
}