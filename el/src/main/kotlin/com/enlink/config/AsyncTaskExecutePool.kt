package com.enlink.config

import com.enlink.config.exception.AsyncExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor


/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/26 10:25
 * @description
 */
@Configuration
@EnableAsync
open class AsyncTaskExecutePool : AsyncConfigurer {

    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.setCorePoolSize(10)
        executor.setMaxPoolSize(100)
        executor.setQueueCapacity(100)
        executor.setThreadNamePrefix("CustomTaskExecutor-")
        executor.initialize()
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        // 自定义异常处理类
        return AsyncExceptionHandler()
    }
}