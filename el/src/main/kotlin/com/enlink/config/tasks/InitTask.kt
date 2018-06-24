package com.enlink.config.tasks

import com.enlink.platform.datetime2string
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * 功能描述：初始化任务，延时5秒，每5秒执行一次
 *
 * @auther changgq
 * @date 2018/6/23 11:28
 * @description 该任务主要用于处理日志模块部署后，自动初始化系统索引
 * 日志模块设置：.log-setting
 * 日志备份记录：.log-backups
 * 日志下载记录：.log-downloads
 * 日志删除记录：.log-deletes
 * 日志恢复记录：.log-recoves
 */
@Component
open class InitTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)

    @Async
    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    open fun run() {
        LOGGER.info("${Date().datetime2string()} 日志初始化任务开始执行......")
        val usedTimes = measureTimeMillis {

        }
        LOGGER.info("${Date().datetime2string()} 日志初始化任务开始执行结束，耗时：$usedTimes ms")
    }


}