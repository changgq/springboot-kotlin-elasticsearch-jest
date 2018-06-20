package com.es.services.impl

import com.es.common.*
import com.es.dao.BaseDao
import com.es.echarts.EchartsData
import com.es.model.ModelContants
import com.es.model.BasePage
import com.es.services.ResourceLogService
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.BucketOrder
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.metrics.max.Max
import org.elasticsearch.search.aggregations.metrics.min.Min
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import org.elasticsearch.search.builder.SearchSourceBuilder
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

    // 完成 【资源统计与分析-访问资源排行-访问资源排行榜】X === 废弃
    override fun resourceVisitRank(rangeCondition: RangeCondition, topCount: Int, sortBy: Boolean): ApiResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(emptyMap(), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("distinct")
                    .script(Script("doc['resource_name.keyword'].value")).size(topCount)
                    .subAggregation(AggregationBuilders.count("_key").field("resource_name.keyword"))
                    .order(BucketOrder.aggregation("_key", false))
            val request = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb)
                            .aggregation(aggs))
            val buckets = highLevelClient.search(request).aggregations.get<Terms>("distinct").buckets
            dataMap = mapOf(
                    "xAxis" to Array<String>(buckets.size, { x -> buckets[x].keyAsString }),
                    "yAxis" to Array<Int>(buckets.size, { x -> buckets[x].docCount.toInt() })
            )
        }
        return ApiResponse(dataMap, null, elapsed_time_)
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

    // 完成 【资源统计与分析-访问资源排行-访问应用类型占比】
    override fun applicationPie(rangeCondition: RangeCondition): ApiResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(emptyMap(), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("distinct")
                    .script(Script("doc['app_type.keyword'].value")).size(Int.MAX_VALUE)
                    .subAggregation(AggregationBuilders.count("_key").field("app_type.keyword"))
                    .order(BucketOrder.aggregation("_key", false))
            val request = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb)
                            .aggregation(aggs))

            val buckets = highLevelClient.search(request).aggregations.get<Terms>("distinct").buckets
            dataMap = buckets.map { it.keyAsString to it.docCount.toInt() }.toMap()
        }
        return ApiResponse(dataMap, null, elapsed_time_)
    }

    // 完成 【资源统计与分析-下载资源排行-下载资源排行榜，资源统计与分析-上传资源排行-上传资源排行榜】
    override fun linkTrafficRank(rangeCondition: RangeCondition, topCount: Int, type: String?): ApiResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(emptyMap(), emptyMap(), listOf(rangeCondition))
            val sumFieldName = when (type) {
                "down" -> "long_downlink_traffic"
                "up" -> "long_uplink_traffic"
                else -> "long_total_traffic"
            }
            val aggs = AggregationBuilders.terms("distinct")
                    .field("resource_name.keyword").size(topCount).order(BucketOrder.aggregation("distinct", false))
                    .subAggregation(AggregationBuilders.sum("_key").field(sumFieldName))
                    .order(BucketOrder.aggregation("_key", false))

            val request = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb)
                            .aggregation(aggs))
            println(request.toString())
            val buckets = highLevelClient.search(request).aggregations.get<Terms>("distinct").buckets
            dataMap = mapOf(
                    "xAxis" to Array<String>(buckets.size, { x -> buckets[x].keyAsString }),
                    "yAxis" to Array<String>(buckets.size, { x -> buckets[x].aggregations.get<Sum>("_key").valueAsString })
            )
        }
        return ApiResponse(dataMap, null, elapsed_time_)
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

    // 完成 【资源统计分析-下载资源排行-下载文件格式占比，资源统计分析-上传资源排行-上传文件格式占比】
    override fun fileFormatPie(rangeCondition: RangeCondition, type: String?): ApiResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(emptyMap(), emptyMap(), listOf(rangeCondition))
            val sumFieldName = when (type) {
                "down" -> "long_downlink_traffic"
                "up" -> "long_uplink_traffic"
                else -> "long_total_traffic"
            }
            val aggs = AggregationBuilders.terms("distinct")
                    .script(Script("doc['keyword_file_format'].value")).size(Int.MAX_VALUE)
                    .subAggregation(AggregationBuilders.sum("_key").field(sumFieldName))
                    .order(BucketOrder.aggregation("_key", false))
            val request = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb)
                            .aggregation(aggs))

            val buckets = highLevelClient.search(request).aggregations.get<Terms>("distinct").buckets
            dataMap = buckets.map { it.keyAsString to it.docCount.toInt() }.toMap()
        }
        return ApiResponse(dataMap, null, elapsed_time_)
    }

    override fun resourceVisitGroupStatics(searchCondition: Condition): ApiResponse {
        var pMap = emptyMap<String, BasePage>()
        var dataList = emptyList<Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(searchCondition.preciseConditions, searchCondition.ambiguousConditions, searchCondition.rangeConditionList)
            val subAgg = AggregationBuilders.terms("distinct")
                    .script(Script("doc['resource_name.keyword'].value + '  ' + doc['uri.keyword'].value"))
                    .subAggregation(AggregationBuilders.min("firstVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                    .subAggregation(AggregationBuilders.max("lastVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                    .subAggregation(AggregationBuilders.sum("totalDownloadTraffic").field("long_downlink_traffic"))
                    .subAggregation(AggregationBuilders.sum("totalUploadTraffic").field("long_uplink_traffic"))
                    .subAggregation(AggregationBuilders.sum("_key").field("long_total_traffic"))
                    .order(listOf(BucketOrder.aggregation("_key", false)))

            val totalReq = SearchRequest().indices(INDICES_NAME).source(SearchSourceBuilder().fetchSource(false).query(qb).aggregation(subAgg))
            val totalResp = highLevelClient.search(totalReq)
            var scrollPager: BasePage = BasePage(searchCondition.currentPage, searchCondition.pageSize, totalHits = totalResp.hits.totalHits)
            pMap = mapOf("page" to scrollPager)

            val beginIndex = (searchCondition.currentPage - 1) * searchCondition.pageSize
            val searchRequest = SearchRequest(INDICES_NAME).source(SearchSourceBuilder().fetchSource(false).query(qb).aggregation(subAgg).from(beginIndex).size(searchCondition.pageSize))
            println(searchRequest.toString())
            val searchResp = highLevelClient.search(searchRequest)
            val terms: Terms = searchResp.getAggregations().get("distinct")
            val buckets = terms.buckets
            dataList = List(buckets.size, { y ->
                var x = y
                mapOf(
                        "resourceName" to buckets.get(x).keyAsString.split("  ")[0],
                        "uri" to buckets.get(x).keyAsString.split("  ")[1],
                        "visitCount" to buckets.get(x).docCount.toInt(),
                        "lastVisitDate" to buckets.get(x).aggregations.get<Max>("lastVisitDate").valueAsString,
                        "firstVisitDate" to buckets.get(x).aggregations.get<Min>("firstVisitDate").valueAsString,
                        "totalDownloadTraffic" to buckets.get(x).aggregations.get<Sum>("totalDownloadTraffic").valueAsString,
                        "totalUploadTraffic" to buckets.get(x).aggregations.get<Sum>("totalUploadTraffic").valueAsString,
                        "totalTraffic" to buckets.get(x).aggregations.get<Sum>("_key").valueAsString
                )
            })
        }
        return ApiResponse(dataList, pMap, elapsed_time_)
    }

    override fun resourceVisitDetails(searchCondition: SearchCondition): ApiResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // 完成 【流量统计与分析-应用资源流量排行-应用资源流量排行榜】
    override fun getTrafficRank(rangeCondition: RangeCondition, topCount: Int, type: String): ApiResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(emptyMap(), emptyMap(), listOf(rangeCondition))
            val aggs = when (type) {
                "user" -> groupByUserNameAndUserGroupAggregation(topCount)
                "resource" -> groupByResourceNameAggregation(topCount)
                else -> groupByResourceNameAggregation(topCount)
            }
            val request = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb)
                            .aggregation(aggs))
            val buckets = highLevelClient.search(request).aggregations.get<Terms>("distinct").buckets
            dataMap = mapOf(
                    "xAxis" to Array<String>(buckets.size, { x -> buckets[x].keyAsString }),
                    "yAxis" to Array<String>(buckets.size, { x -> buckets[x].aggregations.get<Sum>("_key").valueAsString })
            )
        }
        return ApiResponse(dataMap, null, elapsed_time_)
    }

    // 完成 【流量统计与分析-用户流量排行-用户流量排行榜】
    override fun getUserFlowAndLoginCount(rangeCondition: RangeCondition, topCount: Int): ApiResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(emptyMap(), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("distinct")
                    .field("user_name.keyword").size(topCount).order(BucketOrder.aggregation("distinct", false))
                    .subAggregation(AggregationBuilders.sum("_key").field("long_total_traffic"))
                    .order(BucketOrder.aggregation("_key", false))
            val request = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb)
                            .aggregation(aggs))
            val buckets = highLevelClient.search(request).aggregations.get<Terms>("distinct").buckets
            dataMap = mapOf(
                    "xAxis" to Array<String>(buckets.size, { x -> buckets[x].keyAsString }),
                    "yAxis" to Array<String>(buckets.size, { x -> buckets[x].aggregations.get<Sum>("_key").valueAsString })
            )
        }
        return ApiResponse(dataMap, null, elapsed_time_)
    }

    // 完成 【流量统计与分析-用户流量排行-用户组流量占比】
    override fun getTrafficPie(rangeCondition: RangeCondition, type: String): ApiResponse {
        val topCount: Int = 100
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(emptyMap(), emptyMap(), listOf(rangeCondition))
            val aggs = when (type) {
                "user" -> groupByUserGroupAggregation(topCount)
                "resource" -> groupByResourceNameAggregation(topCount)
                else -> groupByResourceNameAggregation(topCount)
            }
            val searchRequest = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder().fetchSource(false).query(qb).size(0).aggregation(aggs))
            val searchResp = highLevelClient.search(searchRequest)
            val terms: Terms = searchResp.getAggregations().get("distinct")
            val buckets = terms.buckets
            dataMap = buckets.map { it.keyAsString to it.aggregations.get<Sum>("_key").valueAsString }.toMap()
        }
        return ApiResponse(dataMap, null, elapsed_time_)
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
                        "totalTraffic" to buckets.get(x).aggregations.get<Sum>("_key").valueAsString
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
                        "totalTraffic" to buckets.get(x).aggregations.get<Sum>("_key").valueAsString
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
                .subAggregation(AggregationBuilders.sum("_key").field("long_total_traffic"))
                .order(BucketOrder.aggregation("_key", false))
    }

    private fun groupByUserNameAndUserGroupDetialAggregation(topCount: Int = 10): AggregationBuilder {
        return AggregationBuilders.terms("distinct")
                .script(Script("doc['user_name.keyword'].value + '  ' + doc['user_group.keyword'].value")).size(topCount)
                .subAggregation(AggregationBuilders.min("firstVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                .subAggregation(AggregationBuilders.max("lastVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                .subAggregation(AggregationBuilders.sum("totalDownloadTraffic").field("long_downlink_traffic"))
                .subAggregation(AggregationBuilders.sum("totalUploadTraffic").field("long_uplink_traffic"))
                .subAggregation(AggregationBuilders.sum("_key").field("long_total_traffic"))
                .order(BucketOrder.aggregation("_key", false))
    }

    private fun groupByResourceNameAndUriDetailAggregation(topCount: Int = 10): AggregationBuilder {
        return AggregationBuilders.terms("distinct")
                .script(Script("doc['resource_name.keyword'].value + '  ' + doc['uri.keyword'].value")).size(topCount)
                .subAggregation(AggregationBuilders.min("firstVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                .subAggregation(AggregationBuilders.max("lastVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                .subAggregation(AggregationBuilders.sum("totalDownloadTraffic").field("long_downlink_traffic"))
                .subAggregation(AggregationBuilders.sum("totalUploadTraffic").field("long_uplink_traffic"))
                .subAggregation(AggregationBuilders.sum("_key").field("long_total_traffic"))
                .order(BucketOrder.aggregation("_key", false))
    }

    private fun groupByResourceNameAggregation(topCount: Int): AggregationBuilder {
        return AggregationBuilders.terms("distinct")
                .script(Script("doc['resource_name.keyword'].value")).size(topCount)
                .subAggregation(AggregationBuilders.sum("_key").field(LogFields.FIELD_MAP.get("totalTraffic")))
                .order(BucketOrder.aggregation("_key", false))
    }

    private fun groupByUserGroupAggregation(topCount: Int): AggregationBuilder {
        return AggregationBuilders.terms("distinct")
                .script(Script("doc['user_group.keyword'].value")).size(topCount)
                .subAggregation(AggregationBuilders.sum("_key").field(LogFields.FIELD_MAP.get("totalTraffic")))
                .order(BucketOrder.aggregation("_key", false))
    }
}