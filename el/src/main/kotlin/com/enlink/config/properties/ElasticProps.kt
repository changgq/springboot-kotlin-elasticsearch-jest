package com.enlink.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/25 13:44
 * @description
 */
@ConfigurationProperties(prefix = "spring.elasticsearch.highclient")
class ElasticProps(
        var uris: ArrayList<String> = arrayListOf("http://localhost:9200"),
        var username: String = "",
        var password: String = ""
)