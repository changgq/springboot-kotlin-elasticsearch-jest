package com.enlink.model.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/26 15:56
 * @description
 */
data class ResDto(
        @Expose
        @SerializedName("keyword_log_level")
        val logLevel: String,
        @Expose
        @SerializedName("log_timestamp")
        val logTimestamp: String,
        @Expose
        @SerializedName("user_id")
        val userId: String,
        @Expose
        @SerializedName("user_name")
        val userName: String,
        @Expose
        @SerializedName("app_type")
        val appType: String,
        @Expose
        @SerializedName("short_request_count")
        val shortRequestCount: String,
        @Expose
        @SerializedName("request_referer")
        val requestReferer: String,
        @Expose
        @SerializedName("float_response_time")
        val floatResponseTime: String,
        @Expose
        @SerializedName("keyword_status")
        val keywordStatus: String,
        @Expose
        @SerializedName("session_id")
        val sessionId: String,
        @Expose
        @SerializedName("keyword_file_format")
        val keywordFileFormat: String,
        @Expose
        @SerializedName("path")
        val path: String,
        @Expose
        @SerializedName("browser_info")
        val browserInfo: String,
        @Expose
        @SerializedName("resource_name")
        val resourceName: String,
        @Expose
        @SerializedName("url_http")
        val urlHttp: String,
        @Expose
        @SerializedName("uri")
        val uri: String,
        @Expose
        @SerializedName("host")
        val host: String,
        @Expose
        @SerializedName("ip_address")
        val ipAddress: String,
        @Expose
        @SerializedName("long_total_traffic")
        val longTotalTraffic: String,
        @Expose
        @SerializedName("long_uplink_traffic")
        val longUplinkTraffic: String,
        @Expose
        @SerializedName("long_downlink_traffic")
        val longDownlinkTraffic: String,
        @Expose
        @SerializedName("tags")
        val tags: Array<String>
) : BaseLogDto()