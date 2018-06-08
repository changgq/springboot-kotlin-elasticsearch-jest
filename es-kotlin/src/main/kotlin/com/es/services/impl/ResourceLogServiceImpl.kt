package com.es.services.impl

import com.es.common.*
import com.es.dao.BaseDao
import com.es.echarts.EchartsData
import com.es.echarts.ResourceLogVisit
import com.es.model.ModelContants
import com.es.model.BasePage
import com.es.services.ResourceLogService
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.BucketOrder
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.metrics.max.Max
import org.elasticsearch.search.aggregations.metrics.min.Min
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

/**
 * 功能描述：资源日志业务层实现
 *
 * @auther changgq
 * @date 2018/5/30 10:28
 * @description
 */
@Service
class ResourceLogServiceImpl(val highLevelClient: RestHighLevelClient) : ResourceLogService {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    val INDICES_NAME = "res"
    val fileTypes = arrayOf("*pdf*", "*caj*", "*doc*", "*exl*", "*xls*")

    override fun resTotalCount(cons: RangeCondition): ApiResponse {
        var totalCount = 0L
        val elapsed_time_ = measureTimeMillis {
            //            totalCount = elasticsearchDao.queryTotalCount(INDICES_NAME,
//                    null, null, listOf<RangeCondition>(cons))
            totalCount = BaseDao.getListTotal(highLevelClient, INDICES_NAME, SearchCondition(listOf(cons)))
        }
        return ApiResponse(mapOf("totalCount" to totalCount))
    }

    override fun resourceVisitRank(rangeCons: RangeCondition, topCount: Int, sortBy: Boolean): ApiResponse {
        var echartsData = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val buckets = getBuckets(null, null,
                    rangeCons, mapOf("resourceName.keyword" to "count"), topCount = topCount, sortIsAsc = sortBy)
            echartsData = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString }),
                    yAxis_Integer = Array<Int>(buckets.size, { x -> buckets.get(x).docCount.toInt() }))
        }
        return ApiResponse(echartsData, elapsed_time_)
    }

    override fun resourceOrderByDownload(rangeCons: RangeCondition, topCount: Int, sortBy: Boolean): ApiResponse {
        var echartsData = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val buckets = getBuckets(null,
                    Array<Map<String, String>>(fileTypes.size, { x -> mapOf("fileFormat" to fileTypes.get(x)) }),
                    rangeCons, mapOf("downloadTraffic" to "sum"), topCount = topCount, sortIsAsc = sortBy)
            echartsData = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString }),
                    yAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).aggregations.get<Sum>("_sum_by").valueAsString }))
        }
        return ApiResponse(echartsData, elapsed_time_)
    }

    override fun resourceOrderByType(rangeCons: RangeCondition, topCount: Int, sortBy: Boolean): ApiResponse {
        var echartsData = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val buckets = getBuckets(null,
                    Array<Map<String, String>>(fileTypes.size, { x -> mapOf("fileFormat" to fileTypes.get(x)) }),
                    rangeCons, mapOf("resourceName.keyword" to "count"), topCount = topCount, sortIsAsc = sortBy)
            echartsData = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString }),
                    yAxis_Integer = Array<Int>(buckets.size, { x -> buckets.get(x).docCount.toInt() }))
        }
        return ApiResponse(echartsData, elapsed_time_)
    }

    override fun applicationPie(rangeCondition: RangeCondition): ApiResponse {
        var echartsData = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val buckets = getBuckets(null, null,
                    rangeCondition, mapOf("appType.keyword" to "count"), "appType.keyword", 0, false)
            echartsData = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString }),
                    yAxis_Integer = Array<Int>(buckets.size, { x -> buckets.get(x).docCount.toInt() }))
        }
        return ApiResponse(echartsData, elapsed_time_)
    }

    override fun linkTrafficRank(rangeCondition: RangeCondition, topCount: Int, type: String?): ApiResponse {
        var echartsData = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val buckets = getBuckets(null, null,
                    rangeCondition, getTrafficType(type), topCount = topCount, sortIsAsc = false)
            echartsData = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString }),
                    yAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).aggregations.get<Sum>("_sum_by").valueAsString }))
        }
        return ApiResponse(echartsData, elapsed_time_)
    }

    private fun getTrafficType(type: String?): Map<String, String> {
        val absQueryCons = when (type) {
            "down" -> {
                mapOf("downloadTraffic" to "sum")
            }
            "up" -> {
                mapOf("uploadTraffic" to "sum")
            }
            else -> {
                mapOf("totalTraffic" to "sum")
            }
        }
        return absQueryCons
    }

    override fun fileFormatPie(rangeCondition: RangeCondition, type: String?): ApiResponse {
        var echartsData = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val buckets = getBuckets(null, null,
                    rangeCondition, getTrafficType(type), "fileFormat.keyword", 0, false)
            echartsData = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString }),
                    yAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).aggregations.get<Sum>("_sum_by").valueAsString }))
        }
        return ApiResponse(echartsData, elapsed_time_)
    }

    override fun resourceVisitGroupStatics(searchCondition: SearchCondition): ApiResponse {
        var scrollPager: BasePage = BasePage(searchCondition.currentPage, searchCondition.pageSize)
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuilders(searchCondition.exactList, searchCondition.dimList, searchCondition.scopeList)
            val subAgg = AggregationBuilders.terms("_groupBy").field(ModelContants.PARAMS_MAPS.get("uri.keyword"))
                    .order(BucketOrder.aggregation("_totalTraffic_sum_by", false)).size(10000)
            subAgg.subAggregation(AggregationBuilders.max("_date_max_by").field(ModelContants.PARAMS_MAPS.get("date")).format("yyyy-MM-dd HH:mm:ss"))
            subAgg.subAggregation(AggregationBuilders.sum("_totalTraffic_sum_by").field(ModelContants.PARAMS_MAPS.get("totalTraffic")))
            subAgg.subAggregation(AggregationBuilders.terms("_resource_name").field(ModelContants.PARAMS_MAPS.get("resourceName.keyword")))

            val req = BaseDao.createSearchRequest(INDICES_NAME, searchCondition, subAgg)
            val searchResp = highLevelClient.search(req)

            val resourceLogVisitList = ArrayList<ResourceLogVisit>()
            val total = searchResp.hits.totalHits
            val terms: Terms = searchResp.getAggregations().get("_groupBy")
            var buckets = terms.buckets
            for (b in buckets) {
                val resourceLogVisit = ResourceLogVisit(b.aggregations.get<Terms>("_resource_name").buckets.get(0).keyAsString,
                        b.keyAsString, b.docCount.toInt(), b.aggregations.get<Max>("_date_max_by").valueAsString,
                        b.aggregations.get<Sum>("_totalTraffic_sum_by").valueAsString)
                resourceLogVisitList.add(resourceLogVisit)
            }
            scrollPager.total = total
//            scrollPager.scrollId = searchResp.scrollId
            scrollPager.data = resourceLogVisitList

            println("total = $total, pageIndex = ${scrollPager.pageIndex}, pageSize = ${scrollPager.pageSize}, \r\n scrollId = ${searchCondition.scrollId}")
            println(GsonUtils.convert(scrollPager.data as ArrayList<ResourceLogVisit>))
        }
        return ApiResponse(scrollPager, elapsed_time_)
    }

    override fun resourceVisitDetails(searchCondition: SearchCondition): ApiResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTrafficRank(rangeCondition: RangeCondition, topCount: Int, type: String): ApiResponse {
        var echart_data_ = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val aggs = when (type) {
                "user" -> groupByUserNameAndUserGroupAggregation(topCount)
                "resource" -> groupByResourceNameAggregation(topCount)
                else -> groupByResourceNameAggregation(topCount)
            }
            val searchRequest = BaseDao.createSearchRequest(INDICES_NAME, SearchCondition(listOf(rangeCondition)), aggs)
            val searchResp = highLevelClient.search(searchRequest)
            val buckets = searchResp.getAggregations().get<Terms>("distinct").buckets
            echart_data_ = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString.split("  ")[0] }),
                    yAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).aggregations.get<Sum>("totalTraffic").valueAsString }))
        }
        return ApiResponse(echart_data_, elapsed_time_)
    }

    override fun getUserFlowAndLoginCount(rangeCondition: RangeCondition, topCount: Int): ApiResponse {
        TODO()

    }

    override fun getTrafficPie(rangeCondition: RangeCondition, type: String): ApiResponse {
        val topCount: Int = 100
        var echart_data_ = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val aggs = when (type) {
                "user" -> groupByUserGroupAggregation(topCount)
                "resource" -> groupByResourceNameAggregation(topCount)
                else -> groupByResourceNameAggregation(topCount)
            }
            val searchRequest = BaseDao.createSearchRequest(INDICES_NAME, SearchCondition(listOf(rangeCondition)), aggs)
            val searchResp = highLevelClient.search(searchRequest)

            val terms: Terms = searchResp.getAggregations().get("distinct")
            val buckets = terms.buckets
            echart_data_ = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString.split("  ")[0] }),
                    yAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).aggregations.get<Sum>("totalTraffic").valueAsString }))
        }
        return ApiResponse(echart_data_, elapsed_time_)
    }

    override fun getTrafficGroupStatics(condition: SearchCondition, type: String): ApiResponse {
        var searchPager = BasePage(condition.currentPage, condition.pageSize)
        val elapsed_time_ = measureTimeMillis {
            val startIndex = (condition.currentPage - 1) * condition.pageSize
            println("startIndex $startIndex")
            val endIndex = condition.currentPage * condition.pageSize

            val aggs = when (type) {
                "user" -> groupByUserNameAndUserGroupDetialAggregation(endIndex)
                "resource" -> groupByResourceNameAndUriDetailAggregation(endIndex)
                else -> groupByResourceNameAndUriDetailAggregation(endIndex)
            }

            val searchRequest = BaseDao.createSearchRequest(INDICES_NAME, condition, aggs)
            val searchResp = highLevelClient.search(searchRequest)

            val terms: Terms = searchResp.getAggregations().get("distinct")
            val buckets = terms.buckets

            searchPager.data = List(buckets.size - startIndex, { y ->
                var x = y + startIndex
                mapOf(
                        "name" to buckets.get(x).keyAsString.split("  ")[0],
                        "name2" to buckets.get(x).keyAsString.split("  ")[1],
                        "lastVisitDate" to buckets.get(x).aggregations.get<Max>("lastVisitDate").valueAsString,
                        "firstVisitDate" to buckets.get(x).aggregations.get<Min>("firstVisitDate").valueAsString,
                        "totalDownloadTraffic" to buckets.get(x).aggregations.get<Sum>("totalDownloadTraffic").valueAsString,
                        "totalUploadTraffic" to buckets.get(x).aggregations.get<Sum>("totalUploadTraffic").valueAsString,
                        "totalTraffic" to buckets.get(x).aggregations.get<Sum>("totalTraffic").valueAsString
                )
            })
        }
        return ApiResponse(searchPager, elapsed_time_)
    }

    override fun getTrafficDetails(condition: SearchCondition, type: String): ApiResponse {
        var searchPager = BasePage(condition.currentPage, condition.pageSize)
        val elapsed_time_ = measureTimeMillis {
            val startIndex = (condition.currentPage - 1) * condition.pageSize
            println("startIndex $startIndex")
            val endIndex = condition.currentPage * condition.pageSize

            val aggs = when (type) {
                "user" -> groupByUserNameAndUserGroupDetialAggregation(endIndex)
                "resource" -> groupByResourceNameAndUriDetailAggregation(endIndex)
                else -> groupByResourceNameAndUriDetailAggregation(endIndex)
            }

            val searchRequest = BaseDao.createSearchRequest(INDICES_NAME, condition, aggs)
            val searchResp = highLevelClient.search(searchRequest)

            val terms: Terms = searchResp.getAggregations().get("distinct")
            val buckets = terms.buckets

            searchPager.data = List(buckets.size - startIndex, { y ->
                var x = y + startIndex
                mapOf(
                        "name" to buckets.get(x).keyAsString.split("  ")[0],
                        "name2" to buckets.get(x).keyAsString.split("  ")[1],
                        "lastVisitDate" to buckets.get(x).aggregations.get<Max>("lastVisitDate").valueAsString,
                        "firstVisitDate" to buckets.get(x).aggregations.get<Min>("firstVisitDate").valueAsString,
                        "totalDownloadTraffic" to buckets.get(x).aggregations.get<Sum>("totalDownloadTraffic").valueAsString,
                        "totalUploadTraffic" to buckets.get(x).aggregations.get<Sum>("totalUploadTraffic").valueAsString,
                        "totalTraffic" to buckets.get(x).aggregations.get<Sum>("totalTraffic").valueAsString
                )
            })
        }
        return ApiResponse(searchPager, elapsed_time_)
    }

    private fun getBuckets(preciseQueryCons: Map<String, Array<String>>? = emptyMap(),
                           fuzzyQueryCons: Array<Map<String, String>>? = emptyArray(),
                           rangeCons: RangeCondition,
                           absQueryCons: Map<String, String> = emptyMap(),
                           groupBy: String = "resourceName.keyword",
                           topCount: Int = 100,
                           sortIsAsc: Boolean = false): List<Terms.Bucket> {
        val aggs = BaseDao.aggBuilders(groupBy, topCount, sortIsAsc, absQueryCons)
        val searchRequest = BaseDao.createSearchRequest(INDICES_NAME, SearchCondition(listOf(rangeCons)), aggs)
        return highLevelClient.search(searchRequest).aggregations.get<Terms>("_groupBy").buckets
    }

    private fun groupByUserNameAndUserGroupAggregation(topCount: Int = 10): AggregationBuilder {
        return AggregationBuilders.terms("distinct")
                .script(Script("doc['user_name.keyword'].value + '  ' + doc['user_group.keyword'].value")).size(topCount)
                .subAggregation(AggregationBuilders.sum("totalTraffic").field("long_total_traffic"))
                .order(BucketOrder.aggregation("totalTraffic", false))
    }

    private fun groupByUserNameAndUserGroupDetialAggregation(topCount: Int = 10): AggregationBuilder {
        return AggregationBuilders.terms("distinct")
                .script(Script("doc['user_name.keyword'].value + '  ' + doc['user_group.keyword'].value")).size(topCount)
                .subAggregation(AggregationBuilders.min("firstVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                .subAggregation(AggregationBuilders.max("lastVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                .subAggregation(AggregationBuilders.sum("totalDownloadTraffic").field("long_downlink_traffic"))
                .subAggregation(AggregationBuilders.sum("totalUploadTraffic").field("long_uplink_traffic"))
                .subAggregation(AggregationBuilders.sum("totalTraffic").field("long_total_traffic"))
                .order(BucketOrder.aggregation("totalTraffic", false))
    }

    private fun groupByResourceNameAndUriDetailAggregation(topCount: Int = 10): AggregationBuilder {
        return AggregationBuilders.terms("distinct")
                .script(Script("doc['resource_name.keyword'].value + '  ' + doc['uri.keyword'].value")).size(topCount)
                .subAggregation(AggregationBuilders.min("firstVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                .subAggregation(AggregationBuilders.max("lastVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                .subAggregation(AggregationBuilders.sum("totalDownloadTraffic").field("long_downlink_traffic"))
                .subAggregation(AggregationBuilders.sum("totalUploadTraffic").field("long_uplink_traffic"))
                .subAggregation(AggregationBuilders.sum("totalTraffic").field("long_total_traffic"))
                .order(BucketOrder.aggregation("totalTraffic", false))
    }

    private fun groupByResourceNameAggregation(topCount: Int): AggregationBuilder {
        return AggregationBuilders.terms("distinct")
                .script(Script("doc['resource_name.keyword'].value")).size(topCount)
                .subAggregation(AggregationBuilders.sum("totalTraffic").field(ModelContants.PARAMS_MAPS.get("totalTraffic")!!))
                .order(BucketOrder.aggregation("totalTraffic", false))
    }

    private fun groupByUserGroupAggregation(topCount: Int): AggregationBuilder {
        return AggregationBuilders.terms("distinct")
                .script(Script("doc['user_group.keyword'].value")).size(topCount)
                .subAggregation(AggregationBuilders.sum("totalTraffic").field(ModelContants.PARAMS_MAPS.get("totalTraffic")!!))
                .order(BucketOrder.aggregation("totalTraffic", false))
    }
}