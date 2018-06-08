package com.es.services

import com.es.model.LogTimedTask

/**
 * 功能描述：定时任务
 *
 * @auther changgq
 * @date 2018/6/7 20:45
 * @description
 */
interface LogTimedTaskService {
    fun insert(task: LogTimedTask): Boolean
    fun update(task: LogTimedTask): Boolean
    fun deleteById(id: String): Boolean
    fun findById(id: String): LogTimedTask
    fun findAll(): List<LogTimedTask>
}