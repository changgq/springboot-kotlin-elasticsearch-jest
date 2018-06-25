package com.enlink.config.tasks

import com.enlink.config.properties.PathProps
import com.enlink.dao.DocumentDao
import com.enlink.dao.IndexDao
import com.enlink.model.LogBackups
import com.enlink.model.LogSetting
import com.enlink.platform.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import java.io.File
import java.util.*

/**
 * 功能描述：每日备份前一天的日志记录到Excel文件，供索引恢复和下载使用
 *
 * @auther changgq
 * @date 2018/6/23 02:25
 * @description
 */
open class DownloadIndexTask {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var indexDao: IndexDao
    @Autowired
    lateinit var documentDao: DocumentDao
    @Autowired
    lateinit var pathProps: PathProps

    @Async
    @Scheduled(cron = "0 30 0 * * ?")
    open fun run() {
        // 判断是否有过日志备份记录
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
                    backups(x.toLowerCase(), _start_date, _end_date)
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
    open fun backups(index: String, startDate: Date, endDate: Date) {
        val date = startDate
        // 查询并备份索引
        val fileName = "$index-${date.date2string_point()}"
        val backupsPath = "${pathProps.backups}$index/$fileName.xlsx"
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
    }
}