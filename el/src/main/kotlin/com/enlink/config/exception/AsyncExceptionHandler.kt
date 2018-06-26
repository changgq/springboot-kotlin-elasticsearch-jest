package com.enlink.config.exception

import com.enlink.platform.GsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import java.lang.reflect.Method


/**
 * 功能描述：异步执行异常处理
 *
 * @auther changgq
 * @date 2018/6/26 10:32
 * @description
 */
class AsyncExceptionHandler : AsyncUncaughtExceptionHandler {
    private val LOGGER: Logger = LoggerFactory.getLogger(AsyncExceptionHandler::class.java)

    override fun handleUncaughtException(ex: Throwable, method: Method, vararg params: Any) {
        LOGGER.info("Async method: {} has uncaught exception, params: {}", method.getName(), GsonUtils.convert(params))

        if (ex is AsyncException) {
            LOGGER.info("asyncException: {}", ex.errorMassage)
        }

        LOGGER.info("Exception :")
        ex.printStackTrace()
    }
}