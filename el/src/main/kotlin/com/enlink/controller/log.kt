package com.enlink.controller

import com.enlink.model.LogSetting
import com.enlink.platform.*
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.BucketOrder
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.charset.Charset
import kotlin.math.log
import kotlin.system.measureTimeMillis

/**
 * 功能描述：日志基础接口
 */
@RestController
@RequestMapping("/log")
class LogController : BaseController() {

    /**
     * 功能描述: 分页查询日志数据
     */
    @RequestMapping(value = "list", method = arrayOf(RequestMethod.POST))
    fun getLogList(@RequestBody condition: Condition, logType: String): CommonResponse {
        return list(condition, logType)
    }

    /**
     * 功能描述: 获取下拉菜单列表中的值
     */
    @RequestMapping(value = "/groupConditions", method = arrayOf(RequestMethod.GET))
    fun getGroupConditions(logType: String, condition: String): CommonResponse {
        var _request = SearchRequest(getIndexName(logType)).source(
                SearchSourceBuilder().fetchSource(false).size(0).aggregation(
                        AggregationBuilders.terms("distinct").field(getFieldName(condition)).size(MAX_AGGR_COUNT)))
        LOGGER.info(_request.toString())
        val resp = client.search(_request)
        val terms = resp.getAggregations().get<Terms>("distinct")
        val list = List<String>(terms.getBuckets().size, { x -> terms.getBuckets().get(x).keyAsString })
        return CommonResponse(list)
    }
}

/**
 * 功能描述：日志设置接口
 */
@RestController
@RequestMapping("/logconfig")
class LogConfigAction : BaseController() {
    private val logConfigPath = "d://usr/local/enlink/logJson.config"

    /**
     * 功能描述: 更新日志设置信息
     * @auther changgq
     * @date 2018/6/8 10:26
     *
     * @param
     * @return
     */
    @RequestMapping("/set", method = arrayOf(RequestMethod.POST))
    fun updateSetting(@RequestBody logSetting: LogSetting): CommonResponse {
        val rel = client.update(UpdateRequest("log_setting", "LOG_SETTING", logSetting.id).doc(logSetting.toMap()))
        // 保存日志文件到服务器
        val f = File(logConfigPath)
        if (!f.exists()) {
            f.parentFile.mkdirs()
        }
        f.writeText(GsonUtils.convert(logSetting)!!, Charset.defaultCharset())
        return CommonResponse(rel.status().status == 200)
    }

    /**
     * 功能描述: 获取日志设置信息
     * @auther changgq
     * @date 2018/6/8 10:25
     *
     * @param
     * @return
     */
    @GetMapping("/get")
    fun get(): CommonResponse {
        val resp = client.get(GetRequest("log_setting", "LOG_SETTING", "1"))
        return CommonResponse(GsonUtils.reConvert(resp.sourceAsString, LogSetting::class.java))
    }
}

@RestController
@RequestMapping("/reslog")
class ResourceLogController : BaseController() {
    private val indicesName = IndexMappings.Index_mappings.get("resLog")

    // 完成 【资源统计与分析-访问资源排行-访问应用类型占比】
    @RequestMapping(value = "/applicationPie", method = arrayOf(RequestMethod.POST))
    fun applicationPie(@RequestBody rangeCondition: RangeCondition): CommonResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(emptyMap(), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("appTypes")
                    .script(Script("doc['app_type.keyword'].value")).size(Int.MAX_VALUE)
                    .subAggregation(AggregationBuilders.count("appTypeCount").field("app_type.keyword"))
                    .order(BucketOrder.aggregation("appTypeCount", false))
            val request = SearchRequest().indices(indicesName)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb).aggregation(aggs))
            LOGGER.info(request.toString())
            val buckets = client.search(request).aggregations.get<Terms>("appTypes").buckets
            dataMap = buckets.map { it.keyAsString to it.docCount.toInt() }.toMap()
        }
        return CommonResponse(dataMap, elapsed_time_)
    }

    // 完成 【资源统计与分析-下载资源排行-下载资源排行榜，资源统计与分析-上传资源排行-上传资源排行榜】
    @RequestMapping(value = "/linkTrafficRank", method = arrayOf(RequestMethod.POST))
    fun linkTrafficRank(@RequestBody rangeCondition: RangeCondition, topCount: Int = 10, type: String? = "total"): CommonResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(emptyMap(), emptyMap(), listOf(rangeCondition))
            val sumFieldName = when (type) {
                "down" -> "long_downlink_traffic"
                "up" -> "long_uplink_traffic"
                else -> "long_total_traffic"
            }
            val aggs = AggregationBuilders.terms("distinct")
                    .field("resource_name.keyword").size(topCount).order(BucketOrder.aggregation("distinct", false))
                    .subAggregation(AggregationBuilders.sum("totalTraffic").field(sumFieldName))
                    .order(BucketOrder.aggregation("totalTraffic", false))

            val request = SearchRequest().indices(indicesName)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb).aggregation(aggs))

            println(request.toString())
            val buckets = client.search(request).aggregations.get<Terms>("distinct").buckets
            dataMap = mapOf(
                    "xAxis" to Array<String>(buckets.size, { x -> buckets[x].keyAsString }),
                    "yAxis" to Array<String>(buckets.size, { x -> buckets[x].aggregations.get<Sum>("totalTraffic").valueAsString })
            )
        }
        return CommonResponse(dataMap, elapsed_time_)
    }
}