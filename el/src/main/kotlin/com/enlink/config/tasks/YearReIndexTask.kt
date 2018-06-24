package com.enlink.config.tasks

import com.enlink.services.IndexService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 功能描述：每年（1月1日凌晨0点）创建年报索引
 *
 * @auther changgq
 * @date 2018/6/23 02:33
 * @description
 */
@Component
open class YearReIndexTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexService: IndexService


    @Async
    @Scheduled(cron = "0 0 0 1 1 ?")
    fun run() {

    }
}