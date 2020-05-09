package com.andrea.groupup.Http.Mapper

import com.andrea.groupup.Models.LocalPlace
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type

class Mapper {
//    fun singleMapper(jsonObject: JSONObject, cls: Class<*>): Any? {
//        return Gson().fromJson(jsonObject.toString(), cls)
//    }

    inline fun <reified T, reified U> mapper (obj: T): U {
        val type = object: TypeToken<U>(){}.type
        return Gson().fromJson(obj.toString(), type)
    }
}