package com.es.api

import com.es.common.ApiResponse
import com.google.gson.Gson
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentType
import kotlin.system.measureTimeMillis

/**
 * 通用Action与工具类
 */
open class CommonAction(val highLevelClient: RestHighLevelClient) {

    /**
     * 通过url获取相应数据
     */
    fun urlGet(method: String, url: String, params: Map<String, String>? = emptyMap()): ApiResponse {
        var resultMap = emptyMap<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val response = highLevelClient.lowLevelClient.performRequest(method, url, params)
            response.entity.content.use { `is` ->
                resultMap = XContentHelper.convertToMap(XContentType.JSON.xContent(), `is`, true)
            }
        }
        return ApiResponse(resultMap, elapsed_time_)
    }

    inline fun <reified T : Any> Gson.fromJson(json: String): T {
        return Gson().fromJson(json, T::class.java)
    }
}