package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants
import org.json.JSONObject

class FriendHttp(val context: Context)  {
    private val URL = Constants.BASE_URL + "/friends"
    private val http = Http(context)

    fun getFriend(id: String, volleyCallbackArray: VolleyCallbackArray){
        http.getAll("$URL/friendlist/$id", volleyCallbackArray)
    }

    fun getFriendStatus(id1: String, id2: String, volleyCallback: VolleyCallback) {
        http.getOne("$URL/status/user1/$id1/user2/$id2", volleyCallback)
    }

    fun getFriendRequests(id: String, volleyCallbackArray: VolleyCallbackArray){
        http.getAll("$URL/requests/$id", volleyCallbackArray)
    }

    fun addToFriend(idUser: String, token: String, volleyCallback: VolleyCallback){
        http.postWithToken("$URL/invite/$idUser", volleyCallback, JSONObject("{}"), token)
    }

    fun acceptRequest(id: String, token: String, volleyCallbackArray: VolleyCallbackArray){
        http.putWithToken("$URL/accept/$id", volleyCallbackArray, token)
    }

    fun removeFriend(id1: String, id2: String, volleyCallback: VolleyCallback){
        http.delete("$URL/$id1/$id2", volleyCallback)
    }
}