package com.enlink.model

import com.enlink.platform.date2string
import java.util.*

/**
 * 功能描述: 用户日志
 */
data class UserLog(val userId: String, val userGroup: String,
                   val userName: String, val userAuth: String, val operation: String,
                   val ipAddress: String, val macAddress: String, val logInfo: String,
                   val logTimeStamp: String, val certificateServer: String, val linkInterface: String)

/**
 * 功能描述: 管理员日志
 */
data class AdminLog(val userId: String, val userGroup: String,
                    val userName: String, val userAuth: String, val operation: String,
                    val ipAddress: String, val macAddress: String, val logInfo: String, val logTimeStamp: String)

/**
 * 功能描述: 资源日志
 */
data class ResLog(val userGroup: String, val userName: String,
                  val userAuth: String, val status: String, val visitAddress: String, val uri: String,
                  val ip: String, val resourceName: String, val urlHttpProtocal: String,
                  val requestCount: String, val uplinkTraffic: String, val downlinkTraffic: String,
                  val totalTraffic: String, val responseTime: String, val logTimeStamp: String, val clientInfo: String)

/**
 * 功能描述: 系统日志
 */
data class SystemLog(val fan: String, val cpu: String,
                     val operateType: String, val systemMsg: String, val logTimeStamp: String)

/**
 * 功能描述: 登录日志
 */
data class LoginLog(val deviceType: String, val deviceOS: String, val clientInfo: String,
                    val operation: String, val status: String, val userId: String,
                    val userGroup: String, val userName: String, val userAuth: String, val certificateServer: String,
                    val linkInterface: String, val macAddress: String, val ipAddress: String)

/**
 * 功能描述: 日志设置信息
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