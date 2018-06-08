package com.es.services

import com.es.model.LogSetting

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/7 19:12
 * @description
 */
interface LogSettingService {
    fun getById(id: String): LogSetting
    fun update(logSetting: LogSetting): Boolean
    fun findAll(): List<LogSetting>
}