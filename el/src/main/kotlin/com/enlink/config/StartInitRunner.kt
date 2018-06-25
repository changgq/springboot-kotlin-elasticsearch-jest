package com.enlink.config

import com.enlink.dao.DocumentDao
import com.enlink.dao.IndexDao
import com.enlink.model.LogSetting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.File
import java.nio.charset.Charset

/**
 * 功能描述：系统启动时自动初始化系统索引，
 * 包括：.log-setting、.log-backups、.log-downloads、.log-deletes、.log-recoves
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
    lateinit var indexDao: IndexDao
    @Autowired
    lateinit var documentDao: DocumentDao

    override fun run(vararg args: String?) {
        // 初始化日志设置、日志删除、日志备份、日志恢复、定时任务配置等相关记录到Elasticsearch
        // 验证以上索引是否存在，若不存在则创建，若存在则跳过
        LOGGER.info("初始化日志模块 start......")
        // 日志模块设置：.log-setting
        // 日志备份记录：.log-backups
        // 日志下载记录：.log-downloads
        // 日志删除记录：.log-deletes
        // 日志恢复记录：.log-recoves
        try {
            val isExists = documentDao.exists(".log-setting", "doc", "1")
            LOGGER.info("索引.log-setting中是否存在文档id为1的记录：$isExists")
            if (!isExists) {
                LOGGER.info("1")
                val indices = arrayOf(".log-setting", ".log-backups", ".log-deletes", ".log-recovers")

                // 1、创建日志设置索引
                indices.forEach { x ->
                    LOGGER.info("初始化索引：$x")
                    indexDao.initIndex(x)
                }
                // 2、初始化.log-setting文档
                LOGGER.info("初始化.log-setting文档")
                documentDao.index(".log-setting", LogSetting().jsonString())

                // 3、初始化模板，包括res-template、user-template、admin-template、system-template
                LOGGER.info("初始化模板，包括res-template、user-template、admin-template、system-template")
                val dir = File(this::class.java.classLoader.getResource("elastic/templates").path)
                if (dir.exists()) {
                    val files = dir.listFiles()
                    files.forEach { f ->
                        LOGGER.info(f.name)
                        val templateName = f.name.replace(".json", "")
                        val rel = indexDao.putTemplate(templateName, f.readText())
                        LOGGER.info("模板创建成功！结果：$rel")
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("初始化日志模块失败，错误原因：${e.message}")
        }
        LOGGER.info("初始化日志模块 end!")
    }
}