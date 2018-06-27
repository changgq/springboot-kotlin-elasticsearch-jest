package com.enlink.config.tasks

import com.enlink.config.properties.PathProps
import com.enlink.dao.DocumentDao
import com.enlink.dao.IndexDao
import com.enlink.model.LogBackups
import com.enlink.model.LogSetting
import com.enlink.platform.*
import com.enlink.services.DownloadService
import com.google.gson.GsonBuilder
import org.apache.poi.xssf.streaming.SXSSFWorkbook
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

                // 更新最后备份时间
                logSetting.lastBackupsDate = _start_date.date2string()
                documentDao.update(".log-setting", logSetting.jsonString())
                // 记录日志文件记录
                _start_date = _start_date + day(1)
            }
            LOGGER.info("日志备份开始结束！")
        }
    }

    @Async
    open fun backups(index: String, startDate: Date) {
        val date = startDate
        // 查询并备份索引
        val fileName = "$index-${date.date2string()}"
        val backupsRecord = documentDao.get(".log-backups", "doc", fileName)
        if (backupsRecord.isNotBlank()) {
            val br = GsonBuilder().excludeFieldsWithoutExposeAnnotation()       // 不导出实体中没有用@Expose注解的属性
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")                   // 序列化时间转化为特定格式
                    .create().fromJson<LogBackups>(backupsRecord, LogBackups::class.java)
            if (null != br && br.backupsStatus) {
                return
            }
        }
        val backupsPath = "${pathProps.backups}$index/$fileName.xlsx"
        try {
            val headers: Map<String, String> = downloadService.getHeaders(index)
            // 数据集合
            val dataArrays = mutableListOf<Array<String>>()
            val xwb: SXSSFWorkbook = SXSSFWorkbook(10000)
            val sheet = xwb.createSheet("sheet1")
            val headerRow = sheet.createRow(0)
            var _keyIndex = 0
            for ((k, v) in headers) {
                headerRow.createCell(_keyIndex++).setCellValue(v)
            }

            var startIndex = 1
            val request = SearchRequest().indices(index)
                    .source(SearchSourceBuilder()
                            .query(QueryBuilders.matchAllQuery())
                            .size(200))
                    .scroll(TimeValue(1, TimeUnit.MINUTES))
            var resp = client.search(request)
            if (resp.hits.hits.size > 0) {
                do {
                    val searchHits = resp.getHits().getHits()
                    searchHits.forEach { sh ->
                        val map = sh.sourceAsMap
                        val dd = headers.keys.map { h -> map.get(h).toString() }.toTypedArray()
                        dataArrays.add(dd)
                    }
//                    ExcelUtils.genExcel(backupsPath, headers, dateArrays, startIndex)
                    if (dataArrays.size > 10000) {
                        for (j in 0.rangeTo(dataArrays.size - 1)) {
                            val row = sheet.createRow(startIndex + j)
                            val d = dataArrays[j]
                            for (k in 0.rangeTo(d.size - 1)) {
                                row.createCell(k).setCellValue(d[k].toString())
                            }
                        }
                        startIndex = startIndex + dataArrays.size
                        // 清楚数据，释放内存
                        dataArrays.clear()
                        println("startIndex ======================================== $startIndex ======= ${Date().datetime2string()}")
                    }
                    resp = client.searchScroll(SearchScrollRequest().scrollId(resp.scrollId).scroll(TimeValue(1, TimeUnit.MINUTES)))
                } while (resp.hits.hits.size > 0)
            }
            if (dataArrays.size > 0) {
                startIndex = startIndex + dataArrays.size
                for (j in 0.rangeTo(dataArrays.size - 1)) {
                    val row = sheet.createRow(startIndex + j)
                    val d = dataArrays[j]
                    for (k in 0.rangeTo(d.size - 1)) {
                        row.createCell(k).setCellValue(d[k].toString())
                    }
                }
                println("startIndex ======================================== $startIndex ======= ${Date().datetime2string()}")
                println("数据处理完成，dataArrays size: ${dataArrays.size}")
                // 清楚数据，释放内存
                dataArrays.clear()
            }
            val df = File(backupsPath)
            if (!df.exists()) {
                df.parentFile.mkdirs()
            }
            xwb.write(df.outputStream())
            xwb.close()

            df.outputStream().close()

            val logBackups = LogBackups(fileName, index, date.date2string(), 1, fileName, backupsPath, df.length(), true)
            documentDao.index(".log-backups", logBackups.jsonString(), "doc", logBackups.id)
        } catch (e: Exception) {
            e.printStackTrace()
            LOGGER.info(e.message)
        }
    }
}