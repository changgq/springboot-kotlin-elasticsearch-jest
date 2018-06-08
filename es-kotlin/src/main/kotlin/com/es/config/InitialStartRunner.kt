package com.es.config

import com.es.common.GsonUtils
import com.es.dao.IndicesDao
import com.es.model.LogSetting
import com.es.model.LogTimedTask
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.File
import java.nio.charset.Charset
import org.springframework.web.bind.annotation.RequestMethod


/**
 * 功能描述：Springboot容器启动后执行数据初始化工作
 *
 * @auther changgq
 * @date 2018/6/7 16:23
 * @description
 */

@Component
@Order(1)
class InitialStartRunner(val highLevelClient: RestHighLevelClient) : CommandLineRunner {
    val LOGGER: Logger = LoggerFactory.getLogger(InitialStartRunner::class.java)

    override fun run(vararg args: String?) {
        // 初始化日志设置、日志删除、日志备份、日志恢复、定时任务配置等相关记录到Elasticsearch
        // 验证以上索引是否存在，若不存在则创建，若存在则跳过
        LOGGER.info("初始化日志模块 start......")
        try {
            // 创建日志设置索引
            val indexName = "log_setting"
            val isExists = checkExistsOrCreateIndex(indexName, highLevelClient)
            if (!isExists) {
                val source = LogSetting().toMap()
                IndicesDao.insertIndex(indexName, indexName.toUpperCase(), "1", source, highLevelClient)
                LOGGER.info("文档 $indexName 插入成功! source = ${GsonUtils.convert(source)}")
            }
            LOGGER.info("=========================================================")
            // 创建日志备份索引
            checkExistsOrCreateIndex("log_backups_record", highLevelClient)
            LOGGER.info("=========================================================")
            // 创建日志恢复索引
            checkExistsOrCreateIndex("log_recover_record", highLevelClient)
            LOGGER.info("=========================================================")
            // 创建日志定时任务索引
            val taskIsExists = checkExistsOrCreateIndex("log_timed_task", highLevelClient)
            if (!taskIsExists) {
                // 初始化任务数据
                val f = File(this::class.java.classLoader.getResource("mappings/init.txt").path)
                if (!f.exists()) throw Exception("任务初始化数据文件不存在")
                val bulkRequest = BulkRequest()
                val bytesArray = f.readBytes()
                bulkRequest.add(bytesArray, 0, bytesArray.size, XContentType.JSON)
                highLevelClient.bulk(bulkRequest)
            }
            LOGGER.info("=========================================================")
            // 创建日志定时任务索引
            checkExistsOrCreateIndex("log_delete_record", highLevelClient)
        } catch (e: Exception) {
            LOGGER.error("初始化日志模块失败，错误原因：${e.message}")
        }
        LOGGER.info("初始化日志模块 end!")
    }

    fun checkExistsOrCreateIndex(indexName: String, highLevelClient: RestHighLevelClient): Boolean {
        val resp = highLevelClient.lowLevelClient.performRequest(RequestMethod.HEAD.name, "/$indexName")
        val isExists = resp.statusLine.statusCode == 200
        if (!isExists) {
            val f = File(this::class.java.classLoader.getResource("mappings/$indexName.json").path)
            if (!f.exists()) throw Exception("Indices $indexName create fial, the mappings file is not exists")
            IndicesDao.createIndex(indexName, f.readText(Charset.defaultCharset()), highLevelClient)
            return isExists
        }
        LOGGER.info("索引 $indexName 已存在! 无需初始化!")
        return isExists
    }
}