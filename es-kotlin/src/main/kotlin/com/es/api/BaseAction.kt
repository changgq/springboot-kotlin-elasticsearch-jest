package com.es.api

import com.es.common.*
import com.es.model.*
import jdk.nashorn.internal.codegen.ObjectClassGenerator.getFieldName
import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.search.SearchHit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

/**
 * 基础Action
 */
open class BaseAction(val highLevelClient: RestHighLevelClient) {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    val INDEX_MAX_VALUE = 90000
    val ROLL_SIZE = 1000

    fun urlGet(method: String, url: String, params: Map<String, String>? = emptyMap()): ApiResponse {
        var resultMap = emptyMap<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val response = highLevelClient.lowLevelClient.performRequest(method, url, params)
            response.entity.content.use { `is` ->
                resultMap = XContentHelper.convertToMap(XContentType.JSON.xContent(), `is`, true)
            }
        }
        return ApiResponse(resultMap, elapsed_time_)
    }


    fun toSearchCondition(cond: Condition): SearchCondition {
        var a: List<QueryCondition> = List<QueryCondition>(cond.ambiguousConditions.keys.size, { x ->
            val k = cond.ambiguousConditions.keys.elementAt(x)
            QueryCondition(LogFields.FIELD_MAP[k]!!, cond.ambiguousConditions[k], QueryCondition.QueryType.DIM)
        })
        var b = List<QueryCondition>(cond.preciseConditions.keys.size, { x ->
            val k = cond.preciseConditions.keys.elementAt(x)
            QueryCondition(LogFields.FIELD_MAP[k]!!, cond.preciseConditions[k], QueryCondition.QueryType.EXACT)
        })
        return SearchCondition(cond.rangeConditionList, a.plus(b), cond.sortConditions, cond.currentPage, cond.pageSize)
    }

    fun covertSearchHit2Model(searchHits: Array<SearchHit>, logType: String): List<BaseLog> {
        val list = List<BaseLog>(searchHits.size, { x ->
            val _source = searchHits.get(x).sourceAsMap
            var _baseLog: BaseLog? = null
            when (logType) {
                "userLog" -> {
                    val userId = "" + _source.get(LogFields.FIELD_MAP.get("userId"))
                    val userGroup = "" + _source.get(LogFields.FIELD_MAP.get("userGroup"))
                    val userName = "" + _source.get(LogFields.FIELD_MAP.get("userName"))
                    val userAuth = "" + _source.get(LogFields.FIELD_MAP.get("userAuth"))
                    val operation = "" + _source.get(LogFields.FIELD_MAP.get("operation"))
                    val ipAddress = "" + _source.get(LogFields.FIELD_MAP.get("ipAddress"))
                    val macAddress = "" + _source.get(LogFields.FIELD_MAP.get("macAddress"))
                    val logInfo = "" + _source.get(LogFields.FIELD_MAP.get("logInfo"))
                    val logTimeStamp = "" + _source.get(LogFields.FIELD_MAP.get("logTimeStamp"))
                    val certificateServer = "" + _source.get(LogFields.FIELD_MAP.get("certificateServer"))
                    val linkInterface = "" + _source.get(LogFields.FIELD_MAP.get("linkInterface"))

                    _baseLog = UserOperationLog(userId, userGroup, userName, userAuth, operation,
                            ipAddress, macAddress, logInfo, logTimeStamp, certificateServer, linkInterface)
                }
                "resourceLog" -> {
                    _baseLog = ResourceLog(
                            "" + _source.get(LogFields.FIELD_MAP.get("userGroup")),
                            "" + _source.get(LogFields.FIELD_MAP.get("userName")),
                            "" + _source.get(LogFields.FIELD_MAP.get("userAuth")),
                            "" + _source.get(LogFields.FIELD_MAP.get("status")),
                            "" + _source.get(LogFields.FIELD_MAP.get("visitAddress")),
                            "" + _source.get(LogFields.FIELD_MAP.get("uri")),
                            "" + _source.get(LogFields.FIELD_MAP.get("ip")),
                            "" + _source.get(LogFields.FIELD_MAP.get("resourceName")),
                            "" + _source.get(LogFields.FIELD_MAP.get("urlHttpProtocal")),
                            "" + _source.get(LogFields.FIELD_MAP.get("requestCount")),
                            "" + _source.get(LogFields.FIELD_MAP.get("uplinkTraffic")),
                            "" + _source.get(LogFields.FIELD_MAP.get("downlinkTraffic")),
                            "" + _source.get(LogFields.FIELD_MAP.get("totalTraffic")),
                            "" + _source.get(LogFields.FIELD_MAP.get("responseTime")),
                            "" + _source.get(LogFields.FIELD_MAP.get("logTimeStamp")),
                            "" + _source.get(LogFields.FIELD_MAP.get("clientInfo"))
                    )
                }
                "systemLog" -> {
                    _baseLog = SystemLog(
                            "" + _source.get("fan"),
                            "" + _source.get("cpu"),
                            "" + _source.get("operate_type"),
                            "" + _source.get("system_msg"),
                            "" + _source.get(LogFields.FIELD_MAP.get("date"))
                    )
                }
                "adminLog" -> {
                    _baseLog = AdministratorOperationLog(
                            "" + _source.get(LogFields.FIELD_MAP.get("userId")),
                            "" + _source.get(LogFields.FIELD_MAP.get("userGroup")),
                            "" + _source.get(LogFields.FIELD_MAP.get("userName")),
                            "" + _source.get(LogFields.FIELD_MAP.get("userAuth")),
                            "" + _source.get(LogFields.FIELD_MAP.get("operation")),
                            "" + _source.get(LogFields.FIELD_MAP.get("ipAddress")),
                            "" + _source.get(LogFields.FIELD_MAP.get("macAddress")),
                            "" + _source.get(LogFields.FIELD_MAP.get("logInfo")),
                            "" + _source.get(LogFields.FIELD_MAP.get("logTimeStamp"))
                    )
                }
                "loginLog" -> {
                    _baseLog = LoginLog(
                            "" + _source.get(LogFields.FIELD_MAP.get("deviceType")),
                            "" + _source.get(LogFields.FIELD_MAP.get("deviceOS")),
                            "" + _source.get(LogFields.FIELD_MAP.get("clientInfo")),
                            "" + _source.get(LogFields.FIELD_MAP.get("operation")),
                            "" + _source.get(LogFields.FIELD_MAP.get("status")),
                            "" + _source.get(LogFields.FIELD_MAP.get("userId")),
                            "" + _source.get(LogFields.FIELD_MAP.get("userGroup")),
                            "" + _source.get(LogFields.FIELD_MAP.get("userName")),
                            "" + _source.get(LogFields.FIELD_MAP.get("userAuth")),
                            "" + _source.get(LogFields.FIELD_MAP.get("certificateServer")),
                            "" + _source.get(LogFields.FIELD_MAP.get("linkInterface")),
                            "" + _source.get(LogFields.FIELD_MAP.get("macAddress")),
                            "" + _source.get(LogFields.FIELD_MAP.get("ipAddress"))
                    )
                }
                else -> {
                    _baseLog = BaseLog()
                }
            }
            _baseLog.message = "" + _source.get(LogFields.FIELD_MAP.get("message"))
            var logLevel = "" + _source.get(LogFields.FIELD_MAP.get("logLevel"))
            _baseLog.logLevel = when (logLevel.toUpperCase().trim()) {
                "ERROR" -> BaseLog.LogLevel.ERROR
                "WARNING" -> BaseLog.LogLevel.WARNING
                "INFO" -> BaseLog.LogLevel.INFO
                else -> BaseLog.LogLevel.ERROR
            }
            _baseLog
        })
        return list
    }

    fun searchRequestBuilder(indexType: String) = when (indexType) {
        "ALL" -> SearchRequest()
        "ENLINK_CUSTOM_ALL" -> SearchRequest().indices("admin", "res", "user", "system")
        else -> SearchRequest().indices(LogFields.LOG_INDEX_MAP.get(indexType))
    }


}
