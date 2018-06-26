package com.enlink.controller

import com.enlink.config.properties.PathProps
import com.enlink.dao.DocumentDao
import com.enlink.dao.IndexDao
import com.enlink.model.*
import com.enlink.platform.*
import com.sun.org.apache.xpath.internal.operations.Bool
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.Index
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors.toMap
import kotlin.math.log
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
import kotlin.system.measureTimeMillis
import java.util.Locale.CHINESE


/**
 * 功能描述：基础Action
 *
 * @auther changgq
 * @date 2018/6/20 11:28
 * @description
 */
open class BaseController {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    // 聚合结果最大数量
    val MAX_AGGR_COUNT = 3000
    // 查询结果最大值，Elasticsearch默认为10000，超出后无法分页
    val MAX_COUNT = 10000

    @Autowired
    lateinit var client: RestHighLevelClient

    /**
     * 功能描述: 执行查询
     */
    fun urlQeuery(method: String, url: String, params: Map<String, String>? = emptyMap()): CommonResponse {
        var data = emptyMap<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            client.lowLevelClient.performRequest(method, url, params).entity.content.use { `is` ->
                data = XContentHelper.convertToMap(XContentType.JSON.xContent(), `is`, true)
            }
        }
        LOGGER.info(GsonUtils.convert(data))
        return CommonResponse(data, elapsed_time_)
    }

    /**
     * 功能描述: 分页查询功能
     * @auther changgq
     * @date 2018/6/20 15:35
     *
     * @param
     * @return
     */
    fun list(condition: Condition, logType: String): CommonResponse {
        val beginIndex = (condition.currentPage - 1) * condition.pageSize
        val pageSize = condition.pageSize
        val q = queryBuilders(condition.preciseConditions, condition.ambiguousConditions, condition.rangeConditionList)
        if (logType.equals("userLog")) {
            q.must(QueryBuilders.existsQuery("user_name"))
        }
        val total = getPageCount(logType, q, null)
        val page = Pager(condition.currentPage, condition.pageSize, total)
        if (total != 0L) {
            val listData = getPageData(logType, q, beginIndex, pageSize, condition.sortConditions, null)
            page.pageDatas = listData
            return CommonResponse(page)
        }
        return CommonResponse(page)
    }

    /**
     * 功能描述: 分页查询-获取总记录数
     */
    fun getPageCount(logType: String, q: QueryBuilder, aggrs: AggregationBuilder?): Long {
        val s = SearchSourceBuilder().fetchSource(false).query(q).size(0)
        if (null != aggrs) {
            s.aggregation(aggrs)
        }
        val _request = SearchRequest(getIndexName(logType)).source(s)
        LOGGER.info(_request.source().toString())
        var totalHits = client.search(_request).hits.totalHits
        if (totalHits > MAX_COUNT) totalHits = MAX_COUNT.toLong()
        return totalHits
    }

    /**
     * 功能描述: 分页查询-获取总记录数
     */
    fun getPageData(logType: String, q: QueryBuilder, beginIndex: Int, pageSize: Int,
                    sortMap: Map<String, SortOrder>?, aggrs: AggregationBuilder?): List<Any> {
        val s = SearchSourceBuilder().query(q).from(beginIndex).size(pageSize).sort("@timestamp", SortOrder.DESC)
        if (null != sortMap) {
            sortMap.map {
                s.sort(it.key, it.value)
            }
        }
        if (null != aggrs) {
            s.aggregation(aggrs)
        }

        val request = SearchRequest(getIndexName(logType)).source(s)
        LOGGER.info(request.source().toString())
        val searchHits = client.search(request).hits.hits
        val list = List<Map<String, Any?>>(searchHits.size, { x ->
            reConvertToMap(searchHits.get(x).sourceAsMap, logType)
        })
        LOGGER.info(GsonUtils.convert(list))
        return list
    }

    /**
     * 功能描述: 获取查询QueryBuilder
     */
    fun queryBuilders(exactMap: Map<String, Array<String>> = emptyMap(),
                      dimMap: Map<String, String> = emptyMap(),
                      scopeList: List<RangeCondition> = emptyList()): BoolQueryBuilder {
        LOGGER.info("exactList = ${GsonUtils.convert(exactMap)}, dimList = ${GsonUtils.convert(dimMap)}, scopeList = ${GsonUtils.convert(scopeList)}")
        val bq = QueryBuilders.boolQuery()
        // 精确查询
        exactMap?.forEach { it ->
            if (it.value.asList().size > 0) {
                it.value.asList().forEach { x ->
                    if (x.isNotBlank()) {
                        bq.filter(QueryBuilders.termsQuery(getFieldName(it.key), x))
                    }
                }
            }
        }
        // 模糊查询
        dimMap?.forEach { it ->
            bq.must(QueryBuilders.matchQuery(getFieldName(it.key), it.value))
        }
        // 范围查询
        scopeList?.forEach { it ->
            when (it.type) {
                RangeCondition.Type.DATE -> {
                    bq.must(QueryBuilders.rangeQuery(getFieldName(it.conditionName)).gte(it.gteValue).lte(it.lteValue)
                            .timeZone(it.timeZone))
                }
                else -> {
                    bq.must(QueryBuilders.rangeQuery(getFieldName(it.conditionName)).gte(it.gteValue).lte(it.lteValue))
                }
            }
        }
        return bq
    }

    /**
     * 功能描述: 获取索引名称
     */
    fun getIndexName(logType: String): String? {
        return IndexMappings.Index_mappings[logType] ?: IndexMappings.Index_mappings["allLog"]
    }

    /**
     * 功能描述: 获取索引字段
     */
    fun getFieldName(fieldKey: String): String? {
        return IndexMappings.Index_field_mappings[fieldKey]
    }


    /**
     * 功能描述: 转化source为Kotlin对象
     */
    fun reConvertToMap(sourceMap: Map<String, Any>, logType: String): Map<String, Any?> {
        val kClazz = when (logType) {
            "userLog" -> UserLog::class
            "resLog" -> ResLog::class
            "systemLog" -> SystemLog::class
            "adminLog" -> AdminLog::class
            "loginLog" -> LoginLog::class
            else -> UserLog::class
        }

        val mp = mutableMapOf<String, Any?>()
        mp.putAll(kClazz.superclasses[0].memberProperties.map { x ->
            x.name to sourceMap.get(IndexMappings.Index_field_mappings.get(x.name))
        }.toMap())

        mp.putAll(kClazz.memberProperties.map { x ->
            val _key = x.name
            var _value = sourceMap.get(IndexMappings.Index_field_mappings.get(x.name))
            if (_key.equals("logTimeStamp") && _value != null) {
                _value = _value as String
                if (!_value.contains("-")) {
                    val formatter = SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z", Locale.ENGLISH)
                    _value = formatter.parse(_value).datetime2string()
                }
            }
            if (_key.equals("userGroup")) {
                "userGroupHex" to _value
                if (_value != null) {
                    _value = _value as String
                    if (_value.contains("\\")) {
                        _value = CommonStringUtils.hexStringToString(_value.replace("\\x", ""))
                    }
                }
            }
            _key to _value
        }.toMap())

        return mp
    }
}