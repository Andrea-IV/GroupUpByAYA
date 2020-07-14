package com.andrea.groupup.Http

import android.content.Context
import android.util.Log
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
        http.getAll("$URL?email=$name", volleyCallbackArray)
    }

    fun login(username: String, password: String, firebase_token: String, volleyCallback: VolleyCallback) {
        val body = JSONObject("{\"username\": \"$username\", \"password\": \"$password\", \"firebase_token\": \"$firebase_token\"}")
        http.post("$URL/login", volleyCallback, body)
    }

    fun facebookLogin(id: String, email: String, firebase_token: String?, volleyCallback: VolleyCallback) {
        val body = JSONObject("{\"email\": \"$email\", \"facebook_id\": \"$id\", \"firebase_token\": \"$firebase_token\"}")
        http.post("$URL/login/facebook", volleyCallback, body)
    }

    fun baseLogin(username: String, password: String, volleyCallback: VolleyCallback) {
        val body = JSONObject("{\"username\": \"$username\", \"password\": \"$password\"}")
        http.post("$URL/login", volleyCallback, body)
    }

    fun createUser(email: String, username: String, password: String, passwordConfirm: String, volleyCallback: VolleyCallback) {
        val body = JSONObject("{\"email\": \"$email\", \"firstname\": \"firstname\", \"lastname\": \"lastname\", \"username\": \"$username\", \"password1\": \"$password\", \"password2\": \"$passwordConfirm\", \"admin\": 0}")
        Log.d("CREATE", body.toString())
        http.post(URL, volleyCallback, body)
    }

    fun createUserFacebook(email: String, username: String, password: String, passwordConfirm: String, facebookId: String, volleyCallback: VolleyCallback) {
        val body = JSONObject("{\"email\": \"$email\", \"firstname\": \"firstname\", \"lastname\": \"lastname\", \"username\": \"$username\", \"password1\": \"$password\", \"password2\": \"$passwordConfirm\", \"facebook_id\": $facebookId,\"admin\": 0}")
        Log.d("CREATE", body.toString())
        http.post(URL, volleyCallback, body)
    }

    fun editUser(token: String, params: String, volleyCallback: VolleyCallback){
        http.putWithTokenAndParams(URL, volleyCallback, JSONObject(params), token)
    }
}