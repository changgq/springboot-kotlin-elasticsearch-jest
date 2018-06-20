package com.es.common

/**
 * Api 响应结果
 */
data class ApiResponse(val data: Any?, val extend: Any? = null,
                       val response_time: Long = 0, val status_code: Int = HttpStatus.OK,
                       var message: String = HttpStatus.responses.get(HttpStatus.OK)!!) {
    init {
        message?.let { message = HttpStatus.responses.get(status_code)!! }
    }
}