package com.es.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "server.rest-api")
class ApiProperties {
    /**
     * 是否日志打印
     */
    var isLogPrintable: Boolean = false
}