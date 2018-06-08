package com.es.services

import com.es.model.LogBackupsRecord
import com.es.model.LogDeleteRecord

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/8 15:42
 * @description
 */
interface LogDeleteRecordService {
    fun insert(model: LogDeleteRecord): Boolean
    fun update(model: LogDeleteRecord): Boolean
    fun deleteById(id: String): Boolean
    fun findById(id: String): LogDeleteRecord
}