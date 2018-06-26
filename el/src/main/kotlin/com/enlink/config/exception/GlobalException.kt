package com.enlink.config.exception

import com.enlink.platform.CommonResponse
import com.enlink.platform.HttpStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.system.measureTimeMillis

/**
 * 全局异常处理
 */
@ControllerAdvice
class GlobalException {
    val LOGGER: Logger = LoggerFactory.getLogger(GlobalException::class.java)

    @ResponseBody
    @ExceptionHandler
    fun processException(e: Exception): CommonResponse {
        val elapsed_time_ = measureTimeMillis {
            LOGGER.error(e.message)
            e.printStackTrace()
            LOGGER.debug("===============================================================")
        }
        return CommonResponse(null, elapsed_time_, HttpStatus.INTERNAL_SERVER_ERROR,
                "[" + HttpStatus.responses[HttpStatus.INTERNAL_SERVER_ERROR] + "]:" + e.message)
    }
}

class AsyncException(val code: Int, val errorMassage: String) : Exception()