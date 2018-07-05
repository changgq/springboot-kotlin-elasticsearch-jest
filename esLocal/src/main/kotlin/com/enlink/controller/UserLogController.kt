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
        var data = mutableListOf<UserLogs>()
        val exponse_time = measureTimeMillis {
            data = userLogsGet(sCondition)
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
        println(userGroups)
        return CommonResponse(userGroups, exposeTimes)
    }


    /**
     * 功能说明：读取文件，转化为对象
     */
    fun userLogsGet(sCondition: UserSearchCondition): MutableList<UserLogs> {
        var af: File = File(systemLogProperties.user)
        if (!sCondition.logDate.date2string().equals(Date().date2string())) {
            af = File(systemLogProperties.user)
        }
        val userLogs = mutableListOf<UserLogs>()
        if (af.exists()) {
            val lines = af.readLines()
            for (line in lines) {
                val al = line.split("|")
                userLogs.add(UserLogs(al[0], al[1], al[2], al[3], al[4], al[5], al[6], al[7], al[8], al[9], al[10], al[11], al[12], al[13], al[14], al[15]))
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

class UserSearchCondition(val logDate: Date, val userName: String = "", val userGroup: String = "", val logLevel: String = "", val logInfo: String = "")
