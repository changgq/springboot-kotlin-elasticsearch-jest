package com.es.model

import com.es.common.date2string
import java.util.*


/**
 * 功能描述：日志设置功能
 *
 * @auther changgq
 * @date 2018/6/4 09:32
 * @description
 */
class LogSetting(var id: String = "1",
                 var useThirdDB: Boolean = false,                           // 是否启用第三方数据库
                 var configType: String = ConfigType.DATE.name,             // 日志清理类型，按日期、按数量
                 var startSaveTime: String = Date().date2string(),          // 日志设置的开始时间（yyyy-MM-dd日期格式）
                 var dayRate: Long = 90,                                    // 时间周期
                 var countRate: Long = 10000,                               // 条数周期，默认10000
                 var lastDeleteDate: String = Date().date2string(),         // 已删除日志的最后一天
                 var lastBackupDate: String = Date().date2string(),         // 最后一次日志备份日期
                 var startStatus: Boolean = false,                          // 日志模块开启状态，默认为false
                 var logTypes: String = "${LogType.ALL}",                   // 日志类别，默认所有
                 var logLevels: String = "${LogLevel.ALL}"                  // 日志级别，默认所有
) {

    enum class ConfigType {
        DATE, COUNT
    }

    enum class LogType {
        ALL, RES, USER, ADMIN, SYSTEM
    }

    enum class LogLevel {
        ALL, INFO, WARNING, ERROR
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
                "id" to id,
                "useThirdDB" to useThirdDB,
                "configType" to configType,
                "startSaveTime" to startSaveTime,
                "dayRate" to dayRate,
                "countRate" to countRate,
                "lastDeleteDate" to lastDeleteDate,
                "lastBackupDate" to lastBackupDate,
                "startStatus" to startStatus,
                "logTypes" to logTypes,
                "logLevels" to logLevels
        )
    }
}