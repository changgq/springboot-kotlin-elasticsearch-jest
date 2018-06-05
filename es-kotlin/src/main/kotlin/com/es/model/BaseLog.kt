package com.es.model


open class BaseLog {
    enum class LogLevel {
        INFO, WARNING, ERROR
    }
    var host: String = ""
    var port: Int = 0
    var path: String = ""
    var message: String = ""
    var matchText: String = ""
    var version: String = ""
    var timestamp: String = ""
    var logLevel: LogLevel = LogLevel.ERROR

    fun setLogLevel(lv: String) {
        if (lv.startsWith("I")) {
            this.logLevel = LogLevel.INFO
        } else if (lv.startsWith("W")) {
            this.logLevel = LogLevel.WARNING
        } else {
            this.logLevel = LogLevel.ERROR
        }
    }
}

