package com.es.api

import com.es.common.ApiResponse
import com.es.common.GlobalError
import com.es.common.RangeCondition
import com.es.echarts.EchartsData
import com.es.model.ModelContants
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.action.support.IndicesOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import kotlin.system.measureTimeMillis
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import java.lang.Integer.MAX_VALUE
import java.util.LinkedHashMap


/**
 * 资源日志相关Api
 * @author changgq
 */
@RestController
@RequestMapping("/resLog")
class ResourceLogs(val highLevelClient: RestHighLevelClient) {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * @api {POST} api/log/resLog/resourceVisitRank 1_获取资源访问排行柱状图数据
     * @apiGroup resLog
     * @apiVersion 1.0.0
     * @apiDescription 获取资源访问排行柱状图数据
     * @apiParam {RangeCondition} rangeCondition 范围条件
     * @apiParam {int} topCount 指定返回结果包含前几名
     * @apiParamExample {String} 请求样例：
     * api/log/resLog/resourceVisitRank?topCount=5
     * @apiParamExample {json} 请求Body样例：
     * {
     * "type": "DATE",
     * "conditionName": "date",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": {
     * "xAxis_String": [
     * "Unique",
     * "CTrip",
     * "Tecent",
     * "Baidu",
     * "Youku"
     * ],
     * "xAxis_Integer": null,
     * "xAxis_Float": null,
     * "yAxis_String": null,
     * "yAxis_Integer": [
     * 88118,
     * 88094,
     * 88085,
     * 88056,
     * 88046
     * ],
     * "yAxis_Float": null
     * },
     * "extend": null
     * }
     */
    @RequestMapping(value = "/resourceVisitRank", method = arrayOf(RequestMethod.POST))
    fun getResourceVisitRank(@RequestBody rangeCondition: RangeCondition, topCount: Int = 5): ApiResponse {
        var echartsData: EchartsData? = null
        val hashMap = LinkedHashMap<String, Int>()
        val elapsed_time_ = measureTimeMillis {
            if (null != rangeCondition && rangeCondition.selfCheck()) else GlobalError(GlobalError.ERROR.PARTIAL_PARAMETER_ERROR)
            // 设置查询条件，若限制范围不为空，则设置查询范围
            val boolQuery = QueryBuilders.boolQuery()
            if (null != rangeCondition) {
                when (rangeCondition.type) {
                    RangeCondition.Type.DATE -> {
                        boolQuery.must(QueryBuilders.rangeQuery(ModelContants.PARAMS_MAPS.get(rangeCondition.conditionName))
                                .gte(rangeCondition.gteValue).lte(rangeCondition.lteValue)
                                .timeZone(rangeCondition.timeZone))
                    }
                }
            }
            val searchRequest = SearchRequest().indices("res")
            val searchSource = SearchSourceBuilder()
            searchSource.query(boolQuery)

            // 不显示源数据
            searchSource.fetchSource(false);
            searchRequest.searchType(SearchType.QUERY_THEN_FETCH)

            val aggs = AggregationBuilders.terms("by_resource_name")
                    .field("resource_name.keyword")
                    .order(Terms.Order.count(false)).size(topCount)
                    .subAggregation(AggregationBuilders.count("resource_name_count").field("resource_name.keyword"))
            searchSource.aggregation(aggs)
            searchRequest.source(searchSource)

            // 打印查询语句
            logger.info(searchRequest.toString())
            val searchResp = highLevelClient.search(searchRequest)

            // 获取分组结果
            val agg: Terms = searchResp.getAggregations().get("by_resource_name")
            val buckets = agg.getBuckets()
            //遍历分组结果集

            var h = 0
            val arrayLength = buckets.size
            echartsData = EchartsData(xAxis_String = Array<String>(arrayLength, { x -> "" }), yAxis_Integer = Array<Int>(arrayLength, { x -> 0 }))
            for (entry in buckets) {
                echartsData!!.xAxis_String.set(h, entry.getKeyAsString())
                echartsData!!.yAxis_Integer.set(h, entry.getDocCount().toInt())
                h++
            }
        }
        return ApiResponse(echartsData, elapsed_time_)
    }



}