package com.enlink.config.tasks

import com.enlink.config.properties.PathProps
import com.enlink.dao.DocumentDao
import com.enlink.dao.IndexDao
import com.enlink.model.LogBackups
import com.enlink.model.LogSetting
import com.enlink.platform.*
import com.enlink.services.DownloadService
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
import kotlin.system.measureTimeMillis

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
    @Autowired
    lateinit var indexDao: IndexDao

    @Async
    @Scheduled(cron = "0 30 0 * * ?")
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
    open fun backups(index_: String, startDate: Date) {
        val date = startDate
        val _index_name = "${index_.toLowerCase()}-${date.date2string_point()}"
        if (!indexDao.existsIndex(_index_name)) {
            return
        }
        LOGGER.info("索引 === $_index_name 开始备份......")
        // 查询并备份索引
        val fileName = "$index_-${date.date2string()}"
        LOGGER.info("fileName === $fileName")
//        val backupsRecord = documentDao.get(".log-backups", "doc", fileName)
//        LOGGER.info("backupsRecord === $backupsRecord")
//        if (backupsRecord.isNotEmpty()) {
//            val br = GsonBuilder().excludeFieldsWithoutExposeAnnotation()       // 不导出实体中没有用@Expose注解的属性
//                    .setDateFormat("yyyy-MM-dd HH:mm:ss")                   // 序列化时间转化为特定格式
//                    .create().fromJson<LogBackups>(backupsRecord, LogBackups::class.java)
//            if (null != br && br.backupsStatus) {
//                return
//            }
//        }
        val backupsPath = "${pathProps.backups}$index_/$fileName.xlsx"
        try {
            var startIndex = 1
            val exponse_times_ = measureTimeMillis {

                val df = File(backupsPath)
                if (!df.exists()) {
                    df.parentFile.mkdirs()
                }
                val headers: Map<String, String> = downloadService.getHeaders(index_)
                // 数据集合
                val dataArrays = mutableListOf<Array<String>>()
                val xwb: SXSSFWorkbook = SXSSFWorkbook(10000)
                val sheet = xwb.createSheet("sheet1")
                val headerRow = sheet.createRow(0)
                var _keyIndex = 0
                for (header in headers) {
                    headerRow.createCell(_keyIndex++).setCellValue(header.value)
                }

                val request = SearchRequest().indices(_index_name)
                        .source(SearchSourceBuilder()
                                .query(QueryBuilders.matchAllQuery())
                                .size(200))
                        .scroll(TimeValue(1, TimeUnit.MINUTES))
                var resp = client.search(request)

                do {
                    if (resp.hits.hits.size > 0) {
                        val searchHits = resp.getHits().getHits()
                        searchHits.forEach { sh ->
                            val map = sh.sourceAsMap
                            val dd = headers.map { h -> map.get(h.key).toString() }.toTypedArray()
                            dataArrays.add(dd)
                        }

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
                        println("startIndex == ${Date().datetime2string()} == $fileName == $startIndex ")
                        // 清楚数据，释放内存
                        dataArrays.clear()
                    }
                    resp = client.searchScroll(SearchScrollRequest().scrollId(resp.scrollId).scroll(TimeValue(1, TimeUnit.MINUTES)))
                } while (resp.hits.hits.size > 0)
                if (dataArrays.size > 0) {
                    startIndex = startIndex + dataArrays.size
                    for (j in 0.rangeTo(dataArrays.size - 1)) {
                        val row = sheet.createRow(startIndex + j)
                        val d = dataArrays[j]
                        for (k in 0.rangeTo(d.size - 1)) {
                            row.createCell(k).setCellValue(d[k].toString())
                        }
                    }
                    println("startIndex == ${Date().datetime2string()} == $fileName == $startIndex ")
                    // 清楚数据，释放内存
                    dataArrays.clear()
                }
                xwb.write(df.outputStream())
                xwb.close()
                val logBackups = LogBackups(fileName, index_, date.date2string(), 1, fileName, backupsPath, df.length(), true)
                documentDao.index(".log-backups", logBackups.jsonString(), "doc", logBackups.id)
            }
            println("数据处理完成，总记录数 size: $startIndex 耗时：${exponse_times_/1000} 秒")
        } catch (e: Exception) {
            e.printStackTrace()
            LOGGER.info(e.message)
        }
    }
}