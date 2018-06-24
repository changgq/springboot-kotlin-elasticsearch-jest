package com.enlink.config.tasks

import com.enlink.services.IndexService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled

/**
 * 功能描述：每日备份前一天的日志记录到Excel文件，供索引恢复和下载使用
 *
 * @auther changgq
 * @date 2018/6/23 02:25
 * @description
 */
open class DownloadIndexTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexService: IndexService


    @Async
    @Scheduled(cron = "0 30 0 * * ?")
    open fun run() {
        // 判断是否有过日志备份记录

        // 若无日志备份记录，则从起始日期开始备份，获取日志起始日期

        // 从日志起始日期开始备份

        // 备份前一天的日志

        // 记录日志文件记录

    }
}