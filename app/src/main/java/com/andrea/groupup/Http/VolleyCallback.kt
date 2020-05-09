package com.andrea.groupup.Http

import com.android.volley.VolleyError
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

interface VolleyCallback {
    fun onResponse(jsonObject: JSONObject): Void
    fun onError(error: VolleyError): Void
}