package com.enlink.controller

import com.enlink.config.SystemLogProperties
import com.enlink.platform.CommonResponse
import com.enlink.platform.date2string
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.util.*
import java.util.stream.Collectors
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/userlog")
open class UserLogController {
    @Autowired
    lateinit var systemLogProperties: SystemLogProperties

    @PostMapping("/get")
    fun search(@RequestBody sCondition: UserSearchCondition): CommonResponse {
        var data = Pagers<UserLogs>()
        val exponse_time = measureTimeMillis {
            data = getLogsPaging(sCondition)
        }
        return CommonResponse(data, exponse_time)
    }

    @PostMapping("/userGroup/get")
    fun userGroupGet(@RequestBody sCondition: UserSearchCondition): CommonResponse {
        var userGroups = mutableListOf<String>()
        val exposeTimes = measureTimeMillis {
            val adminMaps = userLogsGet(sCondition)
            userGroups = adminMaps.stream().map(UserLogs::userGroup).distinct().collect(Collectors.toList())
        }
        return CommonResponse(userGroups, exposeTimes)
    }

    /**
     * 功能说明：分页
     */
    fun getLogsPaging(sCondition: UserSearchCondition): Pagers<UserLogs> {
        val pagers = Pagers<UserLogs>(0, sCondition.pageSize, sCondition.currentPage)
        val logs = userLogsGet(sCondition)
        val startIndex = (sCondition.currentPage - 1) * sCondition.pageSize
        val endIndex = sCondition.currentPage * sCondition.pageSize
        pagers.total = logs.size
        pagers.data = logs.subList(if (startIndex < logs.size) startIndex else 0, if (endIndex < logs.size) endIndex else (logs.size))
        return pagers
    }


    /**
     * 功能说明：读取文件，转化为对象
     */
    fun userLogsGet(sCondition: UserSearchCondition): MutableList<UserLogs> {
        var af: File = File(systemLogProperties.user)
        if (!sCondition.logDate.date2string().equals(Date().date2string())) {
            af = File(systemLogProperties.user)
        }
        var userLogs = mutableListOf<UserLogs>()
        if (af.exists()) {
            val lines = af.readLines()
            var data = mutableListOf<UserLogs>()
            for (x in lines) {
                if (x.isNotBlank()) {
                    val logArrays = x.split("|")
                    val ul = UserLogs(logArrays[0].trim(),
                            logArrays[1].trim(),
                            logArrays[2].trim(),
                            logArrays[3].trim(),
                            logArrays[4].trim(),
                            logArrays[5].trim(),
                            logArrays[6].trim(),
                            logArrays[7].trim(),
                            logArrays[8].trim(),
                            logArrays[9].trim(),
                            logArrays[10].trim(),
                            logArrays[11].trim(),
                            logArrays[12].trim(),
                            logArrays[13].trim(),
                            logArrays[14].trim(),
                            logArrays[15].trim())
                    var isChecked = true
                    if (sCondition.logInfo.isNotBlank() && ul.logInfo.isNotBlank()) {
                        isChecked = ul.logInfo.startsWith(sCondition.logInfo)
                    }
                    if (sCondition.logLevel.isNotBlank()) {
                        isChecked = sCondition.logLevel.equals(ul.logLevel)
                    }
                    if (sCondition.userName.isNotBlank()) {
                        isChecked = sCondition.userName.equals(ul.userName)
                    }
                    if (sCondition.userGroup.isNotBlank()) {
                        isChecked = sCondition.userGroup.equals(ul.userGroup)
                    }
                    if (isChecked) {
                        userLogs.add(ul)
                    }
                }
            }
        }
        return userLogs
    }
}

data class UserLogs(
        val logLevel: String,
        val timestamp: String,
        val operation: String,
        val keywordStatus: String,
        val logInfo: String,
        val userId: String,
        val userName: String,
        val userGroup: String,
        val userAuth: String,
        val ipAddress: String,
        val certificateServer: String,
        val linkInterface: String,
        val deviceType: String,
        val deviceOs: String,
        val clientInfo: String,
        val macAddress: String)

class UserSearchCondition(val logDate: Date,
                          val userName: String = "",
                          val userGroup: String = "",
                          val logLevel: String = "",
                          val logInfo: String = "",
                          val currentPage: Int = 1,
                          val pageSize: Int = 10)
