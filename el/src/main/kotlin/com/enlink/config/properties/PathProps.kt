package com.enlink.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/25 13:43
 * @description
 */
@ConfigurationProperties(prefix = "system.path")
class PathProps(
        var tmp: String = "/tmp/",
        var backups: String = "/backups/",
        var config: String = "/usr/local/enlink/logJson.config"
)