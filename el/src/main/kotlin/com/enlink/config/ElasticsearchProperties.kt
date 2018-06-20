package com.enlink.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/20 11:14
 * @description
 */
@ConfigurationProperties(prefix = "spring.elasticsearch.highclient")
class ElasticsearchProperties(
        val uris: ArrayList<String> = arrayListOf("http://localhost:9200"),
        val username: String? = "",
        val password: String? = ""
)