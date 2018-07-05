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
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/systemlog")
class SystemLogController {
    @Autowired
    lateinit var systemLogProperties: SystemLogProperties

    @PostMapping("/get")
    fun search(@RequestBody sCondition: SystemSearchCondition): CommonResponse {
        var data = Pagers<SystemLogs>()
        val exponse_time = measureTimeMillis {
            data = getLogsPaging(sCondition)
        }
        return CommonResponse(data, exponse_time)
    }

    /**
     * 功能说明：分页
     */
    fun getLogsPaging(sCondition: SystemSearchCondition): Pagers<SystemLogs> {
        val pagers = Pagers<SystemLogs>(0, sCondition.pageSize, sCondition.currentPage)
        val logs = readFileContents(sCondition)
        val startIndex = (sCondition.currentPage - 1) * sCondition.pageSize
        val endIndex = sCondition.currentPage * sCondition.pageSize
        pagers.total = logs.size
        pagers.data = logs.subList(if (startIndex < logs.size) startIndex else 0, if (endIndex < logs.size) endIndex else (logs.size))
        return pagers
    }

    /**
     * 功能说明：读取文件，转化为对象
     */
    fun readFileContents(sCondition: SystemSearchCondition): MutableList<SystemLogs> {
        var af = File("${systemLogProperties.system}/${sCondition.logDate.date2string()}.log")
        var systemLogsList = mutableListOf<SystemLogs>()
        if (af.exists()) {
            val lines = af.readLines()
            systemLogsList = lines.map { x ->
                val systemLogArrays = x.split("|")
                SystemLogs(systemLogArrays[0].trim(),
                        systemLogArrays[1].toUpperCase().trim(),
                        systemLogArrays[2].trim(),
                        systemLogArrays[3].trim(),
                        systemLogArrays[4].trim(),
                        systemLogArrays[5].trim(),
                        systemLogArrays[6].trim(),
                        systemLogArrays[7].trim(),
                        systemLogArrays[8].trim(),
                        systemLogArrays[9].trim(),
                        systemLogArrays[10].trim(),
                        systemLogArrays[11].trim())
            }.toMutableList()
        }
        var data = mutableListOf<SystemLogs>()
        for (al in systemLogsList) {
            var isChecked = true
            if (sCondition.logLevel.isNotBlank()) {
                isChecked = sCondition.logLevel.equals(al.logLevel)
            }
            if (isChecked) {
                data.add(al)
            }
        }
        return data
    }
}

class SystemSearchCondition(val logDate: Date,
                            val logLevel: String = "",
                            val currentPage: Int = 1,
                            val pageSize: Int = 10)

data class SystemLogs(
        val dateTime: String,
        val logLevel: String,
        val cpu: String,
        val memory: String,
        val disk: String,
        val temperature: String,
        val fan: String,
        val overloadWarning: String,
        val systemMsg: String,
        val operateType: String,
        val serverStatus: String,
        val processStatus: String)