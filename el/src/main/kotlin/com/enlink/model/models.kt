package com.enlink.model

/**
 * 功能描述: 该文件为初始创建的Bean，后期代码优化或可能删除
 * @auther changgq
 * @date 2018/6/26 16:09
 */

open class BaseLog {
    var host: String = ""
    var port: Int = 0
    var path: String = ""
    var message: String = ""
    var matchText: String = ""
    var version: String = ""
    var timestamp: String = ""
    var logLevel: LogLevel = LogLevel.ERROR

    enum class LogLevel {
        INFO, WARNING, ERROR
    }

    fun setLogLevel(v: String) = when (v) {
        "INFO" -> logLevel = LogLevel.INFO
        "WARNING" -> logLevel = LogLevel.WARNING
        else -> logLevel = LogLevel.ERROR
    }
}


/**
 * 功能描述: 用户日志
 */
data class UserLog(val userId: String, val userGroup: String,
                   val userName: String, val userAuth: String, val operation: String,
                   val ipAddress: String, val macAddress: String, val logInfo: String,
                   val logTimeStamp: String, val certificateServer: String, val linkInterface: String,
                   val deviceType: String
) : BaseLog()

/**
 * 功能描述: 管理员日志
 */
data class AdminLog(val userId: String, val userGroup: String,
                    val userName: String, val userAuth: String, val operation: String,
                    val ipAddress: String, val macAddress: String, val logInfo: String, val logTimeStamp: String
) : BaseLog()

/**
 * 功能描述: 资源日志
 */
data class ResLog(val userGroup: String, val userName: String,
                  val userAuth: String, val status: String, val visitAddress: String, val uri: String,
                  val ip: String, val resourceName: String, val urlHttpProtocal: String,
                  val requestCount: String, val uplinkTraffic: String, val downlinkTraffic: String,
                  val totalTraffic: String, val responseTime: String, val logTimeStamp: String, val clientInfo: String
) : BaseLog()

/**
 * 功能描述: 系统日志
 */
data class SystemLog(val fan: String, val cpu: String,
                     val operateType: String, val systemMsg: String, val logTimeStamp: String
) : BaseLog()

/**
 * 功能描述: 登录日志
 */
data class LoginLog(val deviceType: String, val deviceOS: String, val clientInfo: String,
                    val operation: String, val status: String, val userId: String,
                    val userGroup: String, val userName: String, val userAuth: String, val certificateServer: String,
                    val linkInterface: String, val macAddress: String, val ipAddress: String
) : BaseLog()