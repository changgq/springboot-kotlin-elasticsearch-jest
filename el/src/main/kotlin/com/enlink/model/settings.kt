package com.enlink.model

import com.enlink.platform.date2string
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.reflect.full.memberProperties

open class Settings() {
    enum class ConfigType {
        DATE, COUNT
    }

    enum class LogType {
        RES, USER, ADMIN, SYSTEM
    }

    enum class LogLevel {
        INFO, WARNING, ERROR
    }

    fun toMap(): Map<String, Any?> {
        return this::class.memberProperties.map { x ->
            x.name to x.call(this)
        }.toMap()
    }

    fun jsonString(pretty: Boolean = false): String {
        val gsonBuilder = GsonBuilder()
        if (pretty) gsonBuilder.setPrettyPrinting()                     // 格式化输出（序列化）
        return gsonBuilder.excludeFieldsWithoutExposeAnnotation()       // 不导出实体中没有用@Expose注解的属性
                .setDateFormat("yyyy-MM-dd HH:mm:ss")                   //序列化时间转化为特定格式
                .create().toJson(this)
    }
}

/**
 * 日志设置对象
 */
class LogSetting(
        @Expose
        val id: String = "1",
        // 是否启用第三方数据库
        @Expose
        @SerializedName("use_third_db")
        var useThirdDB: Boolean = false,
        // 日志清理类型，按日期、按数量
        @Expose
        @SerializedName("config_type")
        var configType: String = ConfigType.DATE.name,
        // 日志设置的开始时间（yyyy-MM-dd日期格式）
        @Expose
        @SerializedName("start_save_date")
        var startSaveDate: String = Date().date2string(),
        // 时间周期
        @Expose
        @SerializedName("day_rate")
        var dayRate: Long = 90,
        // 条数周期，默认10000
        @Expose
        @SerializedName("count_rate")
        var countRate: Long = 10000,
        // 已删除日志的最后一天
        @Expose
        @SerializedName("last_delete_date")
        val lastDeleteDate: String = Date().date2string(),
        // 最后一次日志备份日期
        @Expose
        @SerializedName("last_backups_date")
        val lastBackupsDate: String = Date().date2string(),
        // 日志模块开启状态，默认为false
        @Expose
        @SerializedName("start_status")
        val startStatus: Boolean = false,
        // 日志类别，默认所有
        @Expose
        @SerializedName("log_types")
        val logTypes: Array<String> = arrayOf(LogType.RES.name, LogType.USER.name, LogType.ADMIN.name, LogType.SYSTEM.name),
        // 日志级别，默认所有
        @Expose
        @SerializedName("log_levels")
        val logLevels: Array<String> = arrayOf(LogLevel.INFO.name, LogLevel.WARNING.name, LogLevel.ERROR.name)
) : Settings()

/**
 * 日志备份对象
 */
class LogBackups(
        @Expose
        val id: String = UUID.randomUUID().toString(),
        @Expose
        @SerializedName("log_type")
        val logType: String,
        @Expose
        @SerializedName("backups_date")
        val backupsDate: String,
        @Expose
        @SerializedName("backups_times")
        val backupsTimes: Long,
        @Expose
        @SerializedName("file_name")
        val fileName: String,
        @Expose
        @SerializedName("file_path")
        val filePath: String,
        @Expose
        @SerializedName("file_size")
        val fileSize: Long,
        @Expose
        @SerializedName("backups_status")
        val backupsStatus: Boolean
) : Settings()

class LogDeletes(
        @Expose
        val id: String = UUID.randomUUID().toString(),
        @Expose
        @SerializedName("log_type")
        val logType: String,
        @Expose
        @SerializedName("index_name")
        val indexName: String,
        @Expose
        @SerializedName("delete_date")
        val deleteDate: String,
        @Expose
        @SerializedName("delete_status")
        val deleteStatus: Boolean
) : Settings()

class LogRecovers(
        @Expose
        val id: String = UUID.randomUUID().toString(),
        @Expose
        @SerializedName("log_type")
        val logType: String,
        @Expose
        @SerializedName("recover_date")
        val recoverDate: String,
        @Expose
        @SerializedName("recover_times")
        val backupsTimes: Long,
        @Expose
        @SerializedName("file_name")
        val fileName: String,
        @Expose
        @SerializedName("file_path")
        val filePath: String,
        @Expose
        @SerializedName("file_size")
        val fileSize: Long,
        @Expose
        @SerializedName("recover_status")
        val recoverStatus: Boolean
) : Settings()