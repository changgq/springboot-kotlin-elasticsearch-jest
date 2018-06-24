package com.enlink.config

import com.enlink.model.LogSetting
import com.enlink.platform.GsonUtils
import com.enlink.services.IndexService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/23 17:31
 * @description
 */
@Component
@Order(2)
class StartInitRunner : CommandLineRunner {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexService: IndexService

    override fun run(vararg args: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        // 初始化日志设置、日志删除、日志备份、日志恢复、定时任务配置等相关记录到Elasticsearch
        // 验证以上索引是否存在，若不存在则创建，若存在则跳过
        LOGGER.info("初始化日志模块 start......")
        // 日志模块设置：.log-setting
        // 日志备份记录：.log-backups
        // 日志下载记录：.log-downloads
        // 日志删除记录：.log-deletes
        // 日志恢复记录：.log-recoves
        val indices = arrayOf(".log-setting", ".log-backups", ".log-deletes", ".log-recoves")
        try {
            // 创建日志设置索引
            indices.forEach { x ->
                indexService.initIndex(x)
            }
        } catch (e: Exception) {
            LOGGER.error("初始化日志模块失败，错误原因：${e.message}")
        }
        LOGGER.info("初始化日志模块 end!")
    }

}