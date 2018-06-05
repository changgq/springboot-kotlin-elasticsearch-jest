package com.es.api.dto

/**
 * 集群基础信息
 */
data class ElasticsearchDto(var cluster_name: String = "elasticsearch", var cluster_uuid: String = "elasticsearch",
                            var nodeName: String = "master", var version: String = "6.2.4", var build: String = "")