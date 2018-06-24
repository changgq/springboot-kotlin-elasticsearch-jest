package com.enlink.config.tasks

import com.enlink.platform.*
import com.enlink.services.IndexService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

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
    val indices = arrayOf("res", "user", "admin", "system")
    val taskRun: Boolean = false
    @Autowired
    lateinit var indexService: IndexService

    @Async
    @Scheduled(cron = "0 0 1 * * ?")
    open fun run() {
        if (taskRun) {
            LOGGER.info("重新分配索引开始......")
            indices.forEach { x ->
                // 获取索引起始和结束时间
                val min_date = Date(indexService.indexMinDate(x))
                val max_date = Date(indexService.indexMaxDate(x))
                // 循环日期，按日期删除并重建索引
                LOGGER.info("索引 $x 的时间：${min_date.date2string()} ~ ${max_date.date2string()}")
                val date = max_date - day(1)
                while (date >= min_date) {
                    // 给所有按日期创建的索引添加别名
                    reindex(x, date)
                }
            }
            LOGGER.info("重新分配索引结束！")
        }
    }


    fun reindex(index: String, date: Date) {
        val destIndex = "$index-${date.date2string_point()}"
        if (!indexService.existsIndex(destIndex)) {
            LOGGER.info("reindex $index to $destIndex ......")
            indexService.reindex(reindexSource(index, destIndex, date.date2string()))
        }
    }

    fun reindexSource(index: String, destIndex: String, time: String): String {
        return GsonUtils.convert(mapOf<String, Any>(
                "source" to mapOf(
                        "index" to index,
                        "query" to mapOf(
                                "match" to mapOf(
                                        "@timestamp" to time
                                )
                        )
                ),
                "dest" to mapOf(
                        "index" to destIndex
                )
        )).toString()
    }
}