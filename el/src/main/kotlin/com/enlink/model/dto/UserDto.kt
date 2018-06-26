package com.enlink.model.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/26 15:45
 * @description
 */
data class UserDto(
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
        @SerializedName("user_group")
        val userGroup: String,
        @Expose
        @SerializedName("keyword_user_auth")
        val userAuth: String,
        @Expose
        @SerializedName("keyword_status")
        val keywordStatus: String,
        @Expose
        @SerializedName("client_info")
        val clientInfo: String,
        @Expose
        @SerializedName("link_interface")
        val linkInterface: String,
        @Expose
        @SerializedName("log_info")
        val logInfo: String,
        @Expose
        @SerializedName("device_os")
        val deviceOs: String,
        @Expose
        @SerializedName("device_type")
        val deviceType: String,
        @Expose
        @SerializedName("host")
        val host: String,
        @Expose
        @SerializedName("port")
        val port: Int,
        @Expose
        @SerializedName("ip_address")
        val ipAddress: String,
        @Expose
        @SerializedName("mac_address")
        val macAddress: String,
        @Expose
        @SerializedName("operation")
        val operation: String,
        @Expose
        @SerializedName("tags")
        val tags: Array<String>
) : BaseLogDto()