package com.es.api.dto

/**
 * 集群基础信息
 */
data class ClusterBasicDto(val cluster_name: String, val cluster_uuid: String, val nodeName: String,
                          val version: String, val build: String)


