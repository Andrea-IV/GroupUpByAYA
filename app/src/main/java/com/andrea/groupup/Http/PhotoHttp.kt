package com.andrea.groupup.Http

import android.content.Context
import com.andrea.groupup.Constants

class PhotoHttp (val context: Context) {
    private val URL = Constants.BASE_URL + "/photos"
    private val http = Http(context)

    fun deletePhoto(photoId: String, volleyCallback: VolleyCallback){
        http.delete("$URL/$photoId", volleyCallback)
    }

}