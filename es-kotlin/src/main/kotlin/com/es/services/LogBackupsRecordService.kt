package com.es.services

import com.es.model.LogBackupsRecord
import com.es.model.LogDeleteRecord

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/7 21:03
 * @description
 */
interface LogBackupsRecordService{
    fun insert(record: LogBackupsRecord): Boolean
    fun update(record: LogBackupsRecord): Boolean
    fun deleteById(id: String): Boolean
    fun findById(id: String): LogBackupsRecord
}