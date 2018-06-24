package com.enlink.config.tasks

import com.enlink.services.IndexService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 功能描述：每周（周一凌晨0点）创建周报索引
 *
 * @auther changgq
 * @date 2018/6/23 02:32
 * @description
 */
@Component
open class WeekReIndexTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexService: IndexService


    @Async
    @Scheduled(cron = "0 0 0 ? * 2")
    fun run() {

    }
}