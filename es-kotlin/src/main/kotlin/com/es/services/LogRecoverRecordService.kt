package com.es.services

import com.es.model.LogDeleteRecord
import com.es.model.LogRecoverRecord

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/7 21:03
 * @description
 */
interface LogRecoverRecordService {
    fun insert(record: LogRecoverRecord): Boolean
    fun update(record: LogRecoverRecord): Boolean
    fun deleteById(id: String): Boolean
    fun findById(id: String): LogRecoverRecord
}