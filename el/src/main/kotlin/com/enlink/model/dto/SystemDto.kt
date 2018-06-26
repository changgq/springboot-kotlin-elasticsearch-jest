package com.enlink.model.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/26 16:04
 * @description
 */
data class SystemDto(
        @Expose
        @SerializedName("keyword_log_level")
        val logLevel: String,
        @Expose
        @SerializedName("log_timestamp")
        val logTimestamp: String,
        @Expose
        @SerializedName("operate_type")
        val operateType: String,
        @Expose
        @SerializedName("memory")
        val memory: String,
        @Expose
        @SerializedName("system_msg")
        val systemMsg: String,
        @Expose
        @SerializedName("cpu")
        val cpu: String,
        @Expose
        @SerializedName("server_status")
        val serverStatus: String,
        @Expose
        @SerializedName("overload_warning")
        val overloadWarning: String,
        @Expose
        @SerializedName("path")
        val path: String,
        @Expose
        @SerializedName("disk")
        val disk: String,
        @Expose
        @SerializedName("fan")
        val fan: String,
        @Expose
        @SerializedName("host")
        val host: String,
        @Expose
        @SerializedName("temperature")
        val temperature: String,
        @Expose
        @SerializedName("tags")
        val tags: Array<String>
) : BaseLogDto()