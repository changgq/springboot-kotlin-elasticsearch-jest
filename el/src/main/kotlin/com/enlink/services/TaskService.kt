package com.enlink.services

import com.enlink.dao.IndexDao
import com.enlink.platform.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * 功能描述：处理定时任务相关的Service
 *
 * @auther changgq
 * @date 2018/6/25 11:04
 * @description
 */
@Component
class TaskService {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexDao: IndexDao

    fun allReindex(indices: String) {
        LOGGER.info("$indices allReindex start ......")
        // 获取索引起始和结束时间
        val min_date = Date(indexDao.indexMinDate(indices))
        val max_date = Date(indexDao.indexMaxDate(indices))
        // 循环日期，按日期删除并重建索引
        LOGGER.info("索引 $indices 的时间：${min_date.date2string()} ~ ${max_date.date2string()}")
        var date = max_date - day(1)
        while (date >= min_date) {
            // 给所有按日期创建的索引添加别名
            reindexByDate(indices, date)
            date = date - day(1)
        }
        LOGGER.info("$indices allReindex end!")
    }

    fun reindexByDate(indices: String, date: Date) {
        LOGGER.info("================== ${date.date2string()} ====================")
        val expend_times = measureTimeMillis {
            // 给所有按日期创建的索引添加别名
            val destIndex = "$indices-${date.date2string_point()}"
//        indexDao.deleteIndex(destIndex)
            if (!indexDao.existsIndex(destIndex)) {
                val source = GsonUtils.convert(mapOf<String, Any>(
                        "source" to mapOf(
                                "index" to indices,
                                "query" to mapOf(
                                        "match" to mapOf(
                                                "@timestamp" to date.date2string()
                                        )
                                )
                        ),
                        "dest" to mapOf(
                                "index" to destIndex
                        )
                )).toString()
                try {
                    indexDao.reindex(source)
                } catch (e: Exception) {
                    LOGGER.error(e.message)
                    e.printStackTrace()
                }
            }
        }
        LOGGER.info("reindex by date ${date.date2string()} start ...... the End! expend times: $expend_times ms.")
    }

}