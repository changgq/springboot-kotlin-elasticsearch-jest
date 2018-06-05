package com.es.model

/**
 * 功能描述：资源日志
 *
 * @auther changgq
 * @date 2018/6/5 15:42
 * @description
 */
data class ResourceLog(
        val userGroup: String = "",
        val userName: String = "",
        val userAuth: String = "",
        val status: String = "",
        val visitAddress: String = "",
        val uri: String = "",
        val ip: String = "",
        val resourceName: String = "",
        val urlHttpProtocal: String = "",
        val requestCount: String = "",
        val uplinkTraffic: String = "",
        val downlinkTraffic: String = "",
        val totalTraffic: String = "",
        val responseTime: String = "",
        val logTimeStamp: String = "",
        val clientInfo: String = ""
) : BaseLog()