package com.es.model

import com.es.common.datetime2string
import java.util.*

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/7 20:12
 * @description
 */
class LogTimedTask(
        var id: String = "",                                        // 主键
        var taskName: String = "",                                  // 任务名称
        var taskCode: String = "",                                  // 任务代码
        var taskCreateDate: String = Date().datetime2string(),      // 任务创建时间
        var taskExecuteTimes: Int = 0,                              // 任务执行频率
        var taskFirstExecute: Long = Date().time,                   // 第一次任务执行时间
        var taskFrequency: String = "",                             // 任务执行频率
        var taskVersion: Int = 0,                                   // 任务版本
        var taskStatus: Boolean = false                             // 任务启动状态，true，false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
                "id" to id,
                "taskName" to taskName,
                "taskCode" to taskCode,
                "taskCreateDate" to taskCreateDate,
                "taskExecuteTimes" to taskExecuteTimes,
                "taskFirstExecute" to taskFirstExecute,
                "taskFrequency" to taskFrequency,
                "taskVersion" to taskVersion,
                "taskStatus" to taskStatus
        )
    }
}