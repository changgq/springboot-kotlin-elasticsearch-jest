package com.enlink.controller

import com.enlink.config.SystemLogProperties
import com.enlink.platform.CommonResponse
import com.enlink.platform.date2string
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.File
import java.util.*
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.streams.toList
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/adminlog")
open class AdminLogController {
    @Autowired
    lateinit var systemLogProperties: SystemLogProperties

    @PostMapping("/get")
    fun search(@RequestBody sCondition: AdminSearchCondition): CommonResponse {
        var data: Pagers<AdminLogs> = Pagers()
        val exponse_time = measureTimeMillis {
            data = getLogsPaging(sCondition)
        }
        return CommonResponse(data, exponse_time)
    }

    @PostMapping("/userGroup/get")
    fun userGroupGet(@RequestBody sCondition: AdminSearchCondition): CommonResponse {
        var userGroups = mutableListOf<String>()
        val exposeTimes = measureTimeMillis {
            val adminMaps = readFileContents(sCondition)
            userGroups = adminMaps.stream().map(AdminLogs::userGroup).distinct().collect(Collectors.toList())
        }
        return CommonResponse(userGroups, exposeTimes)
    }


    /**
     * 功能说明：分页
     */
    fun getLogsPaging(sCondition: AdminSearchCondition): Pagers<AdminLogs> {
        val pagers = Pagers<AdminLogs>(0, sCondition.pageSize, sCondition.currentPage)
        val logs = readFileContents(sCondition)
        val startIndex = (sCondition.currentPage - 1) * sCondition.pageSize
        val endIndex = sCondition.currentPage * sCondition.pageSize
        pagers.total = logs.size
        pagers.data = logs.subList(if (startIndex < logs.size) startIndex else 0, if (endIndex < logs.size) endIndex else (logs.size))
        return pagers
    }

    fun readFileContents(sCondition: AdminSearchCondition): MutableList<AdminLogs> {
        var af: File = File(systemLogProperties.admin)
        if (!sCondition.logDate.date2string().equals(Date().date2string())) {
            af = File(systemLogProperties.admin)
        }
        var adminMaps = mutableListOf<AdminLogs>()
        if (af.exists()) {
            val lines = af.readLines()
            adminMaps = lines.map { x ->
                val adminLogArrays = x.split("|")
                AdminLogs(adminLogArrays[0].trim(),
                        adminLogArrays[1].toUpperCase().trim(),
                        adminLogArrays[2].trim(),
                        adminLogArrays[3].trim(),
                        adminLogArrays[4].trim(),
                        adminLogArrays[5].trim(),
                        adminLogArrays[6].trim(),
                        adminLogArrays[7].trim(),
                        adminLogArrays[8].trim(),
                        adminLogArrays[9].trim())
            }.toMutableList()
        }

        var data = mutableListOf<AdminLogs>()
        for (al in adminMaps) {
            var isChecked = true
            if (sCondition.logInfo.isNotBlank() && al.logInfo.isNotBlank()) {
                isChecked = al.logInfo.startsWith(sCondition.logInfo)
            }
            if (sCondition.logLevel.isNotBlank()) {
                isChecked = sCondition.logLevel.equals(al.logLevel)
            }
            if (sCondition.userName.isNotBlank()) {
                isChecked = sCondition.userName.equals(al.userName)
            }
            if (sCondition.userGroup.isNotBlank()) {
                isChecked = sCondition.userGroup.equals(al.userGroup)
            }
            if (isChecked) {
                data.add(al)
            }
        }
        return data
    }
}

class AdminSearchCondition(val logDate: Date,
                           val userName: String = "",
                           val userGroup: String = "",
                           val logLevel: String = "",
                           val logInfo: String = "",
                           val currentPage: Int = 1,
                           val pageSize: Int = 10)

data class Pagers<T>(var total: Int = 0, var pageSize: Int = 10, var currentPage: Int = 1, var data: List<T> = emptyList())

data class AdminLogs(
        val logLevel: String,
        val timestamp: String,
        val operation: String,
        val userId: String,
        val userName: String,
        val userGroup: String,
        val userAuth: String,
        val ipAddress: String,
        val macAddress: String,
        val logInfo: String)