package com.enlink.config.tasks

import com.enlink.services.IndexService
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
    lateinit var indexService: IndexService


    @Async
    @Scheduled(cron = "0 0 1 * * ?")
    fun run() {
        // 获取索引起始和结束时间

        // 循环日期，按日期删除并重建索引

        // 给所有按日期创建的索引添加别名

    }

}