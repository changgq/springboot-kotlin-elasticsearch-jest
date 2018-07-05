package com.enlink.controller

import com.enlink.config.SystemLogProperties
import com.enlink.platform.CommonResponse
import com.enlink.platform.date2string
import com.enlink.platform.string2datetime
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
@RequestMapping("/reslog")
class ResLogController {
    @Autowired
    lateinit var systemLogProperties: SystemLogProperties

    @PostMapping("/get")
    fun search(@RequestBody sCondition: ResSearchCondition): CommonResponse {
        var data = Pagers<ResLogs>()
        val exponse_time = measureTimeMillis {
            data = getLogsPaging(sCondition)
        }
        return CommonResponse(data, exponse_time)
    }

    @PostMapping("/userGroup/get")
    fun userGroupGet(@RequestBody sCondition: ResSearchCondition): CommonResponse {
        var userGroups = mutableListOf<String>()
        val exposeTimes = measureTimeMillis {
            val adminMaps = resLogsGet(sCondition)
            userGroups = adminMaps.stream().map(ResLogs::userGroup).distinct().collect(Collectors.toList())
        }
        return CommonResponse(userGroups, exposeTimes)
    }

    @PostMapping("/resourceName/get")
    fun resourceNameGet(@RequestBody sCondition: ResSearchCondition): CommonResponse {
        var userGroups = mutableListOf<String>()
        val exposeTimes = measureTimeMillis {
            val adminMaps = resLogsGet(sCondition)
            userGroups = adminMaps.stream().map(ResLogs::resourceName).distinct().collect(Collectors.toList())
        }
        return CommonResponse(userGroups, exposeTimes)
    }

    /**
     * 功能说明：分页
     */
    fun getLogsPaging(sCondition: ResSearchCondition): Pagers<ResLogs> {
        val pagers = Pagers<ResLogs>(0, sCondition.pageSize, sCondition.currentPage)
        val logs = resLogsGet(sCondition)
        val startIndex = (sCondition.currentPage - 1) * sCondition.pageSize
        val endIndex = sCondition.currentPage * sCondition.pageSize
        pagers.total = logs.size
        pagers.data = logs.subList(if (startIndex < logs.size) startIndex else 0, if (endIndex < logs.size) endIndex else (logs.size))
        return pagers
    }


    /**
     * 功能说明：读取文件，转化为对象
     */
    fun resLogsGet(sCondition: ResSearchCondition): MutableList<ResLogs> {
        var af = File("${systemLogProperties.res}")
        var resLogsList = mutableListOf<ResLogs>()
        if (af.exists()) {
            val lines = af.readLines()
            for (x in lines) {
                if (x.isNotBlank()) {
                    val systemLogArrays = x.split("|")
                    val rl = ResLogs(systemLogArrays[0].trim(),
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
                            systemLogArrays[11].trim(),
                            systemLogArrays[12].trim(),
                            systemLogArrays[13].trim(),
                            systemLogArrays[14].trim(),
                            systemLogArrays[15].trim(),
                            systemLogArrays[16].trim(),
                            systemLogArrays[17].trim(),
                            systemLogArrays[18].trim(),
                            systemLogArrays[19].trim())
                    if (null != sCondition.logDate && sCondition.logDate.date2string().equals(rl.timestamp.string2datetime().date2string())) {
                        var isChecked = true
                        if (sCondition.resourceName.isNotBlank() && rl.resourceName.isNotBlank()) {
                            isChecked = rl.resourceName.contains(sCondition.resourceName)
                        }
                        if (sCondition.logLevel.isNotBlank()) {
                            isChecked = sCondition.logLevel.equals(rl.logLevel)
                        }
                        if (sCondition.userName.isNotBlank()) {
                            isChecked = sCondition.userName.equals(rl.userName)
                        }
                        if (sCondition.userGroup.isNotBlank()) {
                            isChecked = sCondition.userGroup.equals(rl.userGroup)
                        }
                        if (isChecked) {
                            resLogsList.add(rl)
                        }
                    }
                }
            }
        }
        return resLogsList
    }
}

class ResSearchCondition(val logDate: Date,
                         val userName: String = "",
                         val userGroup: String = "",
                         val logLevel: String = "",
                         val resourceName: String = "",
                         val currentPage: Int = 1,
                         val pageSize: Int = 10)

// %{DATA:keyword_log_level}\
// |%{DATA:session_id}\
// |%{DATA:user_id}\
// |%{DATA:user_name}\
// |%{DATA:user_group}\
// |%{DATA:ip_address}\
// |%{DATA:keyword_status}\
// |%{DATA:log_timestamp}\
// |%{DATA:float_response_time}\
// |%{DATA:resource_name}\
// |%{DATA:uri}\
// |%{NUMBER:long_uplink_traffic}\
// |%{NUMBER:long_downlink_traffic}\
// |%{NUMBER:long_total_traffic}\
// |%{DATA:browser_info}\
// |%{DATA:request_referer}\
// |%{DATA:short_request_count}\
// |%{DATA:url_http}\
// |%{DATA:keyword_file_format}\
// |%{DATA:app_type}\|
data class ResLogs(
        val logLevel: String,
        val sessionId: String,
        val userId: String,
        val userName: String,
        val userGroup: String,
        val ipAddress: String,
        val keywordStatus: String,
        val timestamp: String,
        val floatResponseTime: String,
        val resourceName: String,
        val uri: String,
        val longUplinkTraffic: String,
        val longDownlinkTraffic: String,
        val longTotalTraffic: String,
        val browserInfo: String,
        val requestReferer: String,
        val shortRequestCount: String,
        val urlHttp: String,
        val keywordFileFormat: String,
        val appType: String)