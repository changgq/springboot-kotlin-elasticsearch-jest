package com.enlink.services

import com.enlink.dao.IndexDao
import com.enlink.platform.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

/**
 * 功能描述：处理定时任务相关的Service
 *
 * @auther changgq
 * @date 2018/6/25 11:04
 * @description
 */
@Service
class TaskService {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexDao: IndexDao

    fun allReindex(indices: String) {
        LOGGER.info("allReindex start ......")
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
        LOGGER.info("allReindex start end!")
    }

    fun reindexByDate(indices: String, date: Date) {
        LOGGER.info("reindex by date ${date.date2string()} start ......")
        // 给所有按日期创建的索引添加别名
        val destIndex = "$indices-${date.date2string_point()}"
        LOGGER.info("reindex $indices to $destIndex !")
//        indexDao.deleteIndex(destIndex)
        if (!indexDao.existsIndex(destIndex)) {
            LOGGER.info("reindex $indices to $destIndex start ......")
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
            indexDao.reindex(source)
        }
        LOGGER.info("reindex by date ${date.date2string()} end!")
    }

}