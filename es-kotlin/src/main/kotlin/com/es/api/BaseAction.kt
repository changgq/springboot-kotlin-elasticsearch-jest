package com.es.api

import com.es.common.ApiResponse
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

/**
 * 基础Action
 */
open class BaseAction(val highLevelClient: RestHighLevelClient) {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)

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
}
