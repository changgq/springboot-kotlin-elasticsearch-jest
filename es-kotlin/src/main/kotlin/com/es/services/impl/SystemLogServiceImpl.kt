package com.es.services.impl

import com.es.common.ApiResponse
import com.es.common.RangeCondition
import com.es.dao.BaseDao
import com.es.date2string_point
import com.es.services.SystemLogService
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.metrics.max.Max
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/1 14:56
 * @description
 */
class SystemLogServiceImpl(val highLevelClient: RestHighLevelClient) : SystemLogService {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun getNetFlowList(): ApiResponse {
        val queryBuilder = BaseDao.queryBuilders(scopeList = listOf(RangeCondition()))
        val aggs = AggregationBuilders.dateHistogram("date").field("@timestamp")
                .format("yyyy-MM-dd HH:mm").timeZone(DateTimeZone.forOffsetHours(8))
                .dateHistogramInterval(DateHistogramInterval.minutes(15))
                .subAggregation(AggregationBuilders.max("lastInNetFlow").field("system.network.in.bytes"))
                .subAggregation(AggregationBuilders.max("lastOutNetFlow").field("system.network.out.bytes"))

        val rq = SearchRequest().indices("metricbeat-*")
                .source(SearchSourceBuilder.searchSource().fetchSource(false).size(0)
                        .query(queryBuilder).aggregation(aggs))
        LOGGER.info(rq.toString())
        val searchResp = highLevelClient.search(rq)
        val buckets = searchResp.aggregations.get<ParsedDateHistogram>("date").buckets

        val data = List(buckets.size, { x ->
            mapOf(
                    "time" to buckets.get(x).keyAsString,
                    "allIn" to buckets.get(x).aggregations.get<Max>("lastInNetFlow").valueAsString.toDouble(),
                    "allOut" to buckets.get(x).aggregations.get<Max>("lastOutNetFlow").valueAsString.toDouble(),
                    "allFlow" to buckets.get(x).aggregations.get<Max>("lastInNetFlow").valueAsString.toDouble() + buckets.get(x).aggregations.get<Max>("lastOutNetFlow").valueAsString.toDouble()
            )
        })

        return ApiResponse(data, 0)
    }

    override fun getNowNetFlow(): ApiResponse {
        val nowDay = Date().date2string_point()
        val queryBuilder = BaseDao.queryBuilders(scopeList = listOf(RangeCondition()))
        val aggs = AggregationBuilders.terms("network").field("system.network.name").size(100)
                .subAggregation(AggregationBuilders.max("time").field("@timestamp").format("yyyy-MM-dd HH:mm"))
                .subAggregation(AggregationBuilders.max("lastInNetFlow").field("system.network.in.bytes"))
                .subAggregation(AggregationBuilders.max("lastOutNetFlow").field("system.network.out.bytes"))

        val rq = SearchRequest().indices("metricbeat-$nowDay")
                .source(SearchSourceBuilder.searchSource().fetchSource(false).size(0)
                        .query(queryBuilder).aggregation(aggs))
        LOGGER.info(rq.toString())
        val searchResp = highLevelClient.search(rq)
        val buckets = searchResp.aggregations.get<Terms>("network").buckets

        var allIn = 0.0
        var allOut = 0.0
        var time = ""
        buckets.forEach { it ->
            time = it.aggregations.get<Max>("time").valueAsString
            allIn += it.aggregations.get<Max>("lastInNetFlow").valueAsString.toDouble()
            allOut += it.aggregations.get<Max>("lastOutNetFlow").valueAsString.toDouble()
        }

        val allFlow = allIn + allOut

        val data = listOf(mapOf(
                "time" to time,
                "allIn" to allIn,
                "allOut" to allOut,
                "allFlow" to allFlow
        ))

        return ApiResponse(data, 0)
    }

}