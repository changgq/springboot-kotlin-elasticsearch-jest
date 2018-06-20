package com.es.model


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

