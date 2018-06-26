package com.enlink.model.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/26 15:47
 * @description
 */
data class AdminDto(
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
        @SerializedName("keyword_user_auth")
        val userAuth: String,
        @Expose
        @SerializedName("path")
        val path: String,
        @Expose
        @SerializedName("host")
        val host: String,
        @Expose
        @SerializedName("port")
        val port: String,
        @Expose
        @SerializedName("ip_address")
        val ipAddress: String,
        @Expose
        @SerializedName("mac_address")
        val macAddress: String,
        @Expose
        @SerializedName("log_info")
        val logInfo: String,
        @Expose
        @SerializedName("operation")
        val operation: String,
        @Expose
        @SerializedName("tags")
        val tags: String
) : BaseLogDto()