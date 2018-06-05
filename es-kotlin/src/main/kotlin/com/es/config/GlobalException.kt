package com.es.config

import com.es.common.ApiResponse
import com.es.common.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.system.measureTimeMillis

/**
 * 全局异常处理
 */
@ControllerAdvice
class GlobalException {
    @ResponseBody
    @ExceptionHandler
    fun processException(e: Exception): ApiResponse {
        val elapsed_time_ = measureTimeMillis {}
        return ApiResponse(null, elapsed_time_, HttpStatus.INTERNAL_SERVER_ERROR,
                "[" + HttpStatus.responses[HttpStatus.INTERNAL_SERVER_ERROR] + "]:" + e.message)
    }
}