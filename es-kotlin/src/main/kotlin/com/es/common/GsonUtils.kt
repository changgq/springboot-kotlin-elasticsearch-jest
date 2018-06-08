package com.es.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

object GsonUtils {
    val gb: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()

    fun convert(any: Any): String? {
        return gb.toJson(any)
    }

    fun <T> reConvert(json: String, clazz: Class<T>?): T {
        return gb.fromJson(json, clazz)
    }

    fun <T> reConvert2List(json: String, type: Type): T {
        return gb.fromJson(json, type)
    }
}