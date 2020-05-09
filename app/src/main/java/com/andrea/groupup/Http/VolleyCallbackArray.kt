package com.andrea.groupup.Http

import com.android.volley.VolleyError
import org.json.JSONArray

interface VolleyCallbackArray {
    fun onResponse(array: JSONArray): Unit
    fun onError(error: VolleyError): Unit
}