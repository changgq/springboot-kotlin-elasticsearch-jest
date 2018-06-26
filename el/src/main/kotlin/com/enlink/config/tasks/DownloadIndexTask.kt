package com.enlink.config.tasks

import com.enlink.config.properties.PathProps
import com.enlink.dao.DocumentDao
import com.enlink.dao.IndexDao
import com.enlink.model.LogBackups
import com.enlink.model.LogSetting
import com.enlink.platform.*
import com.enlink.services.DownloadService
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 功能描述：每日备份前一天的日志记录到Excel文件，供索引恢复和下载使用
 *
 * @auther changgq
 * @date 2018/6/23 02:25
 * @description
 */
@Component
open class DownloadIndexTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var client: RestHighLevelClient
    @Autowired
    lateinit var documentDao: DocumentDao
    @Autowired
    lateinit var pathProps: PathProps
    @Autowired
    lateinit var downloadService: DownloadService

    @Async
    @Scheduled(cron = "0 10 0 * * ?")
    open fun run() {
        // 判断是否有过日志备份记录
        LOGGER.info("=============================================")
        val rel = documentDao.get(".log-setting")
        val logSetting = GsonUtils.reConvert(rel, LogSetting::class.java)
        if (logSetting.startStatus) {
            LOGGER.info("日志备份开始......")
            // 若无日志备份记录，则从起始日期开始备份，获取日志起始日期
            var _start_date = if (logSetting.lastBackupsDate.isBlank()) logSetting.startSaveDate.string2date() else logSetting.lastBackupsDate.string2date()
            val _end_date = Date() - day(1)
            while (_start_date <= _end_date) {
                // 从日志起始日期开始备份 or 备份前一天的日志
                logSetting.logTypes.forEach { x ->
                    backups(x.toLowerCase(), _start_date)
                }
                // 记录日志文件记录
                _start_date = _start_date + day(1)
            }
            logSetting.lastBackupsDate = _end_date.date2string()
            documentDao.update(".log-setting", logSetting.jsonString())
            LOGGER.info("日志备份开始结束！")
        }
    }

    @Async
    open fun backups(index: String, startDate: Date) {
        val date = startDate
        // 查询并备份索引
        val fileName = "$index-${date.date2string()}"
        val backupsPath = "${pathProps.backups}$index/$fileName.xlsx"
        try {
//            val tempPath = this::class.java.classLoader.getResource("/temps/${index}Template.xlsx").path
            var startIndex = 2
            val request = SearchRequest().indices(index)
                    .source(SearchSourceBuilder()
                            .query(QueryBuilders.matchQuery("@timestamp", date.date2string())).size(200))
                    .scroll(TimeValue(1, TimeUnit.MINUTES))
            var resp = client.search(request)
            var total = resp.hits.hits.size
            if (total > 0) {
                val df = File(backupsPath)
                if (!df.exists()) {
                    df.parentFile.mkdirs()
//                    File(tempPath).copyRecursively(df)
                }
                do {
                    val searchHits = resp.getHits().getHits()
                    val dateArrays = mutableListOf<Array<String>>()
                    val headers = downloadService.getHeaders(index)
                    searchHits.forEach { sh ->
                        val map = sh.sourceAsMap
                        val dd = headers.map { h -> map.get(h).toString() }.toTypedArray()
                        dateArrays.add(dd)
                    }
                    ExcelUtils.genExcel(backupsPath, headers, dateArrays, startIndex)
                    resp = client.searchScroll(SearchScrollRequest().scrollId(resp.scrollId).scroll(TimeValue(1, TimeUnit.MINUTES)))
                    total = resp.hits.hits.size
                    startIndex = startIndex + dateArrays.size
                } while (total > 0)
            }
            val backupsStatus = true
            val f = File(backupsPath)
            val logBackups = LogBackups(UUID.randomUUID().toString(),
                    index,
                    date.date2string(),
                    1,
                    fileName,
                    backupsPath,
                    f.length(),
                    backupsStatus)
            documentDao.index(".log-backups", logBackups.jsonString(), "doc", logBackups.id)
        } catch (e: Exception) {
            e.printStackTrace()
            LOGGER.info(e.message)
        }
    }
}