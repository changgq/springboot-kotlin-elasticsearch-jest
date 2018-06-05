package com.es.config

import com.es.common.GsonUtils
import com.es.common.LogSetting
import com.es.common.minus
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.reindex.DeleteByQueryAction
import org.elasticsearch.index.reindex.DeleteByQueryRequest
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import org.elasticsearch.index.reindex.BulkByScrollResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest





/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/4 09:39
 * @description
 */
@Component
open class LogScheduledTasks(val highLevelClient: RestHighLevelClient) {
    val LOGGER = LoggerFactory.getLogger(LogScheduledTasks::class.java)!!
    private val dateFormat = SimpleDateFormat("HH:mm:ss")
    private val dateFormat2 = SimpleDateFormat("yyyy-MM-dd")
    private val logConfigPath = "/usr/local/enlink/logJson.config"
    private var logSetting = LogSetting()

    @Async
    @Scheduled(fixedRate = 100000)
    open fun reportCurrentTime() {
        if (highLevelClient != null) {
            LOGGER.info("现在时间, ${dateFormat.format(Date())}")

        }
    }

    fun deleteDocByDate() {
        val _startDate = dateFormat2.parse(logSetting.startSaveTime)
        val _startDateNew = Date().minus(logSetting.dayRate.toInt())
        val startDate = if (_startDateNew.before(_startDate)) _startDate else _startDateNew
        val request = DeleteByQueryRequest().types("admin", "res", "user", "system").searchRequest
                .source(SearchSourceBuilder()
                        .query(QueryBuilders.rangeQuery("@timestamp").lte(startDate).timeZone("+0800")))
        val response = highLevelClient.search(request)
    }

    fun getLogSetting(): LogSetting {
        val f = File(logConfigPath)
        f.forEachLine(action = ::println)
        if (f.exists()) {
            logSetting = GsonUtils.reConvert(f.readText(), LogSetting::class.java)
        }
        return logSetting
    }
}
