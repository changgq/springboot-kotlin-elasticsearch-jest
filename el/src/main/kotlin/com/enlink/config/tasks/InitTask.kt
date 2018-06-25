package com.enlink.config.tasks

import com.enlink.config.properties.PathProps
import com.enlink.dao.DocumentDao
import com.enlink.model.LogSetting
import com.enlink.platform.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * 功能描述：初始化任务，延时5秒，每5秒执行一次
 *
 * @auther changgq
 * @date 2018/6/23 11:28
 * @description 该任务主要用于处理日志模块部署后，自动初始化系统索引
 * 日志模块设置：.log-setting
 * 日志备份记录：.log-backups
 * 日志下载记录：.log-downloads
 * 日志删除记录：.log-deletes
 * 日志恢复记录：.log-recoves
 */
@Component
@EnableConfigurationProperties(PathProps::class)
open class InitTask(val props: PathProps) {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var documentDao: DocumentDao

    @Async
    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    open fun run() {
        LOGGER.info("${Date().datetime2string()} 日志初始化任务开始执行......")
        val usedTimes = measureTimeMillis {
            checkLogSetting()
        }
        LOGGER.info("${Date().datetime2string()} 日志初始化任务开始执行结束，耗时：$usedTimes ms")
    }

    /**
     * 功能描述: 检测日志设置信息是否有变更
     */
    fun checkLogSetting() {
        // {"configType":"DATE","countRate":0,"dayRate":90,"startSaveTime":"2018-05-13T16:00:00.000Z","useThirdDB":false}
        val f = File(props.config)
        if (f.exists()) {
            val jsonString = f.readText()
            val map: Map<*, *> = GsonUtils.reConvert(jsonString, Map::class.java)
            if (null != map) {
                val logSetting = GsonUtils.reConvert(documentDao.get(".log-setting"), LogSetting::class.java)
                logSetting.configType = map.get("configType") as String
                logSetting.countRate = (map.get("countRate") as Double).toLong()
                logSetting.dayRate = (map.get("dayRate") as Double).toLong()
                logSetting.startSaveDate = (map.get("startSaveTime") as String).replace("Z", " UTC").string2datetime("yyyy-MM-dd'T'HH:mm:ss.SSSZ").date2string()
                logSetting.useThirdDB = map.get("useThirdDB") as Boolean
                documentDao.update(".log-setting", logSetting.jsonString())
            }
        }
    }
}