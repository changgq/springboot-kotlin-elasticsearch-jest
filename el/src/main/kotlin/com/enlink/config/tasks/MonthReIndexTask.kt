package com.enlink.config.tasks

import com.enlink.dao.IndexDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 功能描述：每月（1日凌晨）创建月报索引
 *
 * @auther changgq
 * @date 2018/6/23 02:31
 * @description
 */

@Component
open class MonthReIndexTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexDao: IndexDao


    @Async
    @Scheduled(cron = "0 0 0 1 * ?")
    open fun run() {

    }
}