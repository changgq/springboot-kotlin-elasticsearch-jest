package com.es.model

import com.es.datetime2string
import java.util.*

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/8 15:14
 * @description
 */
class LogDeleteRecord(
        var id: String,                                         // 主键
        var logType: String = "",                               // 日志类型
        var indexName: String = "",                             // 索引名称
        var deleteDate: String = Date().datetime2string(),      // 删除日期
        var deleteStatus: Boolean = true                        // 删除状态，成功：true，失败：false
) {

    fun toMap(): Map<String, Any> {
        return mapOf(
                "id" to id,
                "logType" to logType,
                "indexName" to indexName,
                "deleteDate" to deleteDate,
                "deleteStatus" to deleteStatus
        )
    }
}