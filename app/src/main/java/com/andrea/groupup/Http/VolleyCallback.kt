package com.andrea.groupup.Http

import com.android.volley.VolleyError
import org.json.JSONObject

interface VolleyCallback {
    fun onResponse(jsonObject: JSONObject): Unit
    fun onError(error: VolleyError): Unit
}