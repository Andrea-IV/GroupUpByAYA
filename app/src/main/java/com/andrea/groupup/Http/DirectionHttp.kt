package com.andrea.groupup.Http

import android.content.Context
import com.android.volley.Response

class DirectionHttp(val context: Context) {
    private val http = Http(context)
    fun getPolyline(url: String, response: Response.Listener<String>, error: Response.ErrorListener) {
        http.getDirectionPolyline(url, response, error)
    }
}