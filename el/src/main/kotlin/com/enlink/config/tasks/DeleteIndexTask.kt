package com.enlink.config.tasks

import com.enlink.services.IndexService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 功能描述：每日凌晨执行删除超出存储范围的日志
 *
 * @auther changgq
 * @date 2018/6/23 02:23
 * @description
 */
@Component
open class DeleteIndexTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexService: IndexService


    @Async
    @Scheduled(cron = "0 10 0 * * ?")
    open fun run() {
        // 获取日志存储范围，与当前日期进行比较，获取需要删除的日期

        // 根据日期删除/关闭索引

    }
}