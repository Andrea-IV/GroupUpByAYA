package com.andrea.groupup.Http

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Http {
    private var context: Context
    private var queue: RequestQueue

    constructor(context: Context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(this.context)
    }

    fun getAll(url: String, callback: VolleyCallbackArray): Unit {
        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response -> run { callback.onResponse(response)} },
            { error -> run { callback.onError(error)} }
        )

        queue.add(request)
    }

    fun getOne(url: String, callback: VolleyCallback): Unit {
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response -> run { callback.onResponse(response)} },
            { error -> run { callback.onError(error)} }
        )

        queue.add(request)
    }

    fun post(url: String, callback: VolleyCallback, params: JSONObject): Unit {
        val request = JsonObjectRequest(Request.Method.POST, url, params,
            { response -> run { callback.onResponse(response)} },
            { error -> run { callback.onError(error)} }
        )

        queue.add(request)
    }

    fun put(url: String, callback: VolleyCallback, params: JSONObject): Unit {
        val request = JsonObjectRequest(Request.Method.PUT, url, params,
            { response -> run { callback.onResponse(response)} },
            { error -> run { callback.onError(error)} }
        )

        queue.add(request)
    }

    fun delete(url: String, callback: VolleyCallback): Unit {
        val request = JsonObjectRequest(Request.Method.DELETE, url, null,
            { response -> run { callback.onResponse(response)} },
            { error -> run { callback.onError(error)} }
        )

        queue.add(request)
    }
}