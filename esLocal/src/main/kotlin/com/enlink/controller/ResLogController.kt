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
import kotlin.streams.toList
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/reslog")
class ResLogController {
    @Autowired
    lateinit var systemLogProperties: SystemLogProperties

    @PostMapping("/get")
    fun search(@RequestBody sCondition: ResSearchCondition): CommonResponse {
        var data = mutableListOf<ResLogs>()
        val exponse_time = measureTimeMillis {
            val systemLogs = resLogsGet(sCondition)
            for (al in systemLogs) {
                var isChecked = true
                if (sCondition.logLevel.isNotBlank()) {
                    isChecked = sCondition.logLevel.equals(al.logLevel)
                }
                if (isChecked) {
                    data.add(al)
                }
            }
        }
        return CommonResponse(data.stream().limit(10).toList(), exponse_time)
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

            }
            resLogsList = lines.map { x ->
                val systemLogArrays = x.split("|")
                ResLogs(systemLogArrays[0].trim(),
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
            }.toMutableList()
        }
        return resLogsList
    }
}

class ResSearchCondition(val logDate: Date, val logLevel: String = "")
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