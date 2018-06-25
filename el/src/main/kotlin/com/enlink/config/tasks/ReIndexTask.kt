package com.enlink.config.tasks

import com.enlink.platform.*
import com.enlink.dao.DocumentDao
import com.enlink.model.LogSetting
import com.enlink.services.TaskService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 功能描述：每日凌晨（1点）执行日志重新分配索引的任务
 *
 * @auther changgq
 * @date 2018/6/23 02:14
 * @description
 */
@Component
open class ReIndexTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var documentDao: DocumentDao
    @Autowired
    lateinit var taskService: TaskService

    @Async
    @Scheduled(cron = "0 12 17 * * ?") // 凌晨1点：0 0 1 * * ?
    open fun run() {
        val rel = documentDao.get(".log-setting")
        val logSetting = GsonUtils.reConvert(rel, LogSetting::class.java)
        if (logSetting.startStatus) {
            LOGGER.info("定时reindex日志索引开始......")
            logSetting.logTypes.forEach { x ->
                taskService.allReindex(x.toLowerCase())
            }
            LOGGER.info("定时reindex日志索引结束！")
        }
    }
}