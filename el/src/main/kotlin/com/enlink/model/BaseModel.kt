package com.enlink.model

import com.google.gson.GsonBuilder
import kotlin.reflect.full.memberProperties

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/26 15:12
 * @description
 */
open class BaseModel {
    // 日志设置的类型
    enum class ConfigType {
        DATE, COUNT
    }

    // 日志类型
    enum class LogType {
        RES, USER, ADMIN, SYSTEM
    }

    // 日志级别
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
                .setDateFormat("yyyy-MM-dd HH:mm:ss")                   // 序列化时间转化为特定格式
                .create().toJson(this)
    }
}