package com.es.model

import com.es.datetime2string
import java.util.*

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/7 20:31
 * @description
 */
class LogBackupsRecord(
        var id: String = "",
        var logType: String = LogSetting.LogType.RES.name,
        var backupsDate: String = Date().datetime2string(),
        var backupTimes: Long = 0,
        var fileName: String = "",
        var filePath: String = "",
        var fileSize: Long = 0,
        var backupStatus: Boolean = true
) {


    init {
        fileName = "${logType}_${backupsDate}_.xlsx"
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
                "id" to id,
                "logType" to logType,
                "backupsDate" to backupsDate,
                "backupTimes" to backupTimes,
                "fileName" to fileName,
                "filePath" to filePath,
                "fileSize" to fileSize,
                "backupStatus" to backupStatus
        )
    }
}