package com.enlink.config.tasks

import com.enlink.dao.IndexDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled

/**
 * 功能描述：每日凌晨（0点）创建日报索引
 *
 * @auther changgq
 * @date 2018/6/23 02:29
 * @description
 */
open class DayReIndexTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexDao: IndexDao


    @Async
    @Scheduled(cron = "0 0 0 1/1 * ? ")
    open fun run() {

    }
}