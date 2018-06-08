package com.es.config

import com.es.common.*
import com.es.dao.BaseDao
import com.es.dao.IndicesDao
import com.es.date2string
import com.es.datetime2string
import com.es.model.LogDeleteRecord
import com.es.model.LogSetting
import com.es.model.LogTimedTask
import com.es.services.LogBackupsRecordService
import com.es.services.LogDeleteRecordService
import com.es.services.LogSettingService
import com.es.services.LogTimedTaskService
import com.es.services.impl.LogBackupsRecordServiceImpl
import com.es.services.impl.LogDeleteRecordServiceImpl
import com.es.services.impl.LogSettingServiceImpl
import com.es.services.impl.LogTimedTaskServiceImpl
import com.google.gson.GsonBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer


/**
 * 功能描述：定时任务
 *
 * @auther changgq
 * @date 2018/6/4 09:39
 * @description
 */
@Component
open class LogScheduledTasks(val highLevelClient: RestHighLevelClient) {
    val LOGGER = LoggerFactory.getLogger(this::class.java)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    val taskTimerMap: Map<String, Timer> = emptyMap()
    val taskVersionMap: Map<String, Int> = emptyMap()
    val logSettingService: LogSettingService = LogSettingServiceImpl(highLevelClient)
    val logTimedTaskService: LogTimedTaskService = LogTimedTaskServiceImpl(highLevelClient)
    val logDeleteRecordService: LogDeleteRecordService = LogDeleteRecordServiceImpl(highLevelClient)
    val logBackupRecordService: LogBackupsRecordService = LogBackupsRecordServiceImpl(highLevelClient)

    @Async
    @Scheduled(cron = "0 0/1 * * * *")
    open fun reportCurrentTime() {
        if (highLevelClient != null) {
            // 获取日志设置信息
            val ls = logSettingService.getById("1")
            LOGGER.info("${Date().datetime2string()} 日志设置信息如下: ${GsonBuilder().setPrettyPrinting().create().toJson(ls)}")
            // 判断日志是否开启，日志存储起始时间是否已经生效
            if (ls.startStatus && Date().after(dateFormat.parse(ls.startSaveTime))) {
                val tasks: List<LogTimedTask> = logTimedTaskService.findAll()
                tasks.forEach { t ->
                    executeTimedTasks(t)
                }
            }
        }
    }

    fun executeTimedTasks(task: LogTimedTask) {
        // 判断任务状态是否为启动
        if (task.taskStatus) {
            val tVerson = taskVersionMap[task.id]
            if (task.taskVersion != tVerson) {
                // 验证是否有已有任务
                var timer = taskTimerMap[task.id]
                if (timer != null) timer.cancel()
                timer = Timer()
                val timedTask = when (task.taskCode) {
                    "backupsTask" -> BackupTimerTask()
                    "recoverTask" -> RecoverTimerTask()
                    "deleteTask" -> DeleteTimerTask(highLevelClient)
                    "aggregatePerDayTask" -> AggregatePerDayTimerTask()
                    else -> DefalutTimedTask()
                }
                timer.scheduleAtFixedRate(timedTask, Date(task.taskFirstExecute!!), task.taskFrequency!!.toLong())
                taskVersionMap.plus(task.id to task.taskVersion)
                taskTimerMap.plus(task.id to timer)
            }
        }
    }

    enum class TaskCode {
        backupsTask,            // 备份任务
        recoverTask,            // 恢复任务
        deleteTask,             // 删除任务
        aggregatePerDayTask     // 定期统计任务
    }

    class DefalutTimedTask : TimerTask() {
        val LOGGER = LoggerFactory.getLogger(this::class.java)
        override fun run() {
            LOGGER.info("default task is Executed !")
        }
    }

    class BackupTimerTask : TimerTask() {
        val LOGGER = LoggerFactory.getLogger(this::class.java)
        override fun run() {
            LOGGER.info("定时备份任务开始执行......")

        }
    }

    class RecoverTimerTask : TimerTask() {
        val LOGGER = LoggerFactory.getLogger(this::class.java)
        override fun run() {
            LOGGER.info("定时恢复任务开始执行......")
        }
    }

    class DeleteTimerTask(val client: RestHighLevelClient) : TimerTask() {
        val LOGGER = LoggerFactory.getLogger(this::class.java)
        val logSettingService = LogSettingServiceImpl(client)
        val logDeleteRecordService: LogDeleteRecordService = LogDeleteRecordServiceImpl(client)
        override fun run() {
            LOGGER.info("定时删除任务开始执行......")
            val ls = logSettingService.getById("1")
            // 计算日志删除的日期范围，最后一次删除日期 ~ （当天 - 日志存储周期）
            // 起始日期：日志存储起始日期/最后一次删除日期
            val _startDateStr = if (ls.lastDeleteDate.isNotBlank()) ls.lastDeleteDate else ls.startSaveTime
            val _startDate = SimpleDateFormat("yyyy-MM-dd").parse(_startDateStr)
            // 结束日期：当前日期 - 日志存储周期
            val _endDate = Date().minus(ls.dayRate.toInt())
            if (_startDate.before(_endDate)) {
                // 当起始时间 < 结束日期时，执行删除任务
                if (ls.dayRate.toInt() != 0) {
                    for (k in 0.rangeTo(ls.dayRate.toInt())) {
                        val indexDate = _startDate.plus(k)
                        val indicesList = ls.logTypes.split(",")
                        indicesList.forEach { x ->
                            val indexName = x + "_" + indexDate.date2string()
                            IndicesDao.deleteIndex(indexName, client)
                            // 保存删除记录
                            logDeleteRecordService.insert(LogDeleteRecord(UUID.randomUUID().toString(), x, indexName, Date().datetime2string(), true))
                            // 更新日志设置信息的最后一次备份时间
                            ls.lastDeleteDate = indexDate.date2string()
                            logSettingService.update(ls)
                        }
                    }
                }
            }
        }
    }

    class AggregatePerDayTimerTask : TimerTask() {
        val LOGGER = LoggerFactory.getLogger(this::class.java)
        override fun run() {
            LOGGER.info("定时统计任务开始执行......")
        }
    }
}
