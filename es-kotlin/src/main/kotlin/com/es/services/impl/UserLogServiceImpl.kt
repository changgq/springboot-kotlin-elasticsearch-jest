package com.es.services.impl

import com.es.common.*
import com.es.dao.BaseDao
import com.es.echarts.EchartsData
import com.es.model.ModelContants
import com.es.model.BasePage
import com.es.services.UserLogService
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.BucketOrder
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.metrics.max.Max
import org.elasticsearch.search.aggregations.metrics.min.Min
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/5/31 13:31
 * @description
 */
@Service
class UserLogServiceImpl(val client: RestHighLevelClient) : UserLogService {
    val INDICES_NAME = "user"

    override fun getUserLoginRank(rangeCondition: RangeCondition, topCount: Int): ApiResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(getExactCond2(), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("distinct")
                    .script(Script("doc['user_name.keyword'].values[0]")).size(topCount)
                    .subAggregation(AggregationBuilders.count("_key").field("user_name.keyword"))
                    .order(BucketOrder.aggregation("_key", false))
            val searchReq = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder().fetchSource(false).query(qb).aggregation(aggs))
            val buckets = client.search(searchReq).aggregations.get<Terms>("distinct").buckets
            // 将查询结果倒叙排列，前段需要的数据结构
            val xAxis = Array(buckets.size, { x -> buckets[x].keyAsString })
            val yAxis = Array(buckets.size, { x -> buckets[x].docCount.toInt() })
            dataMap = mapOf("xAxis" to xAxis, "yAxis" to yAxis)
        }
        return ApiResponse(dataMap, null, elapsed_time_)
    }

    override fun getUserLoginRankAndFlow(rangeCondition: RangeCondition, topCount: Int): ApiResponse {
        val userLoginCounts = getUserLoginRank(rangeCondition, topCount)
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val uMap = userLoginCounts.data as Map<String, Any>
            val xAxis: Array<String> = uMap.get("xAxis") as Array<String>
            val xAxis_str = Array<String>(xAxis.size, { x ->
                val map = mapOf<String, Array<String>>("userName.keyword" to arrayOf(xAxis.get(x)))
                val qb = BaseDao.queryBuildersOld(map, emptyMap(), listOf(rangeCondition))
                val aggs = AggregationBuilders.sum("_key").field("long_total_traffic")
                val total = client.search(SearchRequest().indices(LogFields.LOG_INDEX_MAP.get("resLog"))
                        .source(SearchSourceBuilder().fetchSource(false).query(qb).aggregation(aggs)))
                        .aggregations.get<Sum>("_key").valueAsString
                total
            })
            dataMap = mapOf("xAxis_flow" to xAxis_str, "xAxis" to xAxis, "yAxis" to uMap.get("yAxis")!!)
        }
        return ApiResponse(dataMap, null, elapsed_time_)
    }

    override fun getTerminalPie(rangeCondition: RangeCondition): ApiResponse {
        var map = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = BaseDao.queryBuildersOld(getExactCond2(), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("deviceOs")
                    .field("device_os.keyword").size(Int.MAX_VALUE)
                    .subAggregation(AggregationBuilders.count("_key").field("device_os.keyword"))
                    .order(BucketOrder.aggregation("_key", false))
            val searchReq = SearchRequest(INDICES_NAME)
                    .source(SearchSourceBuilder().query(qb).aggregation(aggs).fetchSource(false))
            val resp = client.search(searchReq)
            val buckets = resp.aggregations.get<Terms>("deviceOs").buckets
            map = buckets.map { it.keyAsString to it.docCount.toInt() }.toMap()
        }
        return ApiResponse(map, null, elapsed_time_)
    }

    override fun getUserLoginCount(condition: SearchCondition, userName: String): ApiResponse {
        var loginCount = 0L
        val elapsed_time_ = measureTimeMillis {
            val bq = QueryBuilders.boolQuery()
            bq.must(QueryBuilders.termsQuery(ModelContants.PARAMS_MAPS.get("status.keyword"), "SUCCESS"))
            bq.must(QueryBuilders.termsQuery(ModelContants.PARAMS_MAPS.get("operation.keyword"), "LOGIN"))
            bq.must(QueryBuilders.termsQuery(ModelContants.PARAMS_MAPS.get("userName.keyword"), userName))

            condition.rangeQueryCons!!.forEach { it ->
                bq.must(QueryBuilders.rangeQuery(ModelContants.PARAMS_MAPS.get(it.conditionName))
                        .gte(it.gteValue).lte(it.lteValue).timeZone(it.timeZone))
            }

            val searchRequest = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder.searchSource().fetchSource(false).size(0).query(bq))

            println(searchRequest.toString())

            val searchResponse = client.search(searchRequest)
            loginCount = searchResponse.hits.totalHits
        }
        return ApiResponse(loginCount, elapsed_time_)
    }

    override fun getUserLoginGroupStatics(condition: Condition): ApiResponse {
        val rangeCondition = if (condition.rangeConditionList.size > 0) condition.rangeConditionList.get(0) else RangeCondition()
        val total = getUserLoginTotal(rangeCondition)
        var searchPager = BasePage(condition.currentPage, condition.pageSize, "", total)
        var loginCount = 0L
        var data: List<Map<String, Any>> = emptyList()
        val elapsed_time_ = measureTimeMillis {
            val bq = BaseDao.queryBuildersOld(getExactCond2(), condition.ambiguousConditions, condition.rangeConditionList)
            val startIndex = (condition.currentPage - 1) * condition.pageSize
            println("startIndex $startIndex")
            val endIndex = condition.currentPage * condition.pageSize

            var aggs = AggregationBuilders.terms("distinct")
                    .script(Script("doc['user_name.keyword'].values[0] + '  ' + doc['user_group.keyword'].values[0]")).size(endIndex)

            aggs.subAggregation(AggregationBuilders.max("_date_max_by").field(ModelContants.PARAMS_MAPS.get("date")).format("yyyy-MM-dd HH:mm:ss"))
            aggs.subAggregation(AggregationBuilders.min("_date_min_by").field(ModelContants.PARAMS_MAPS.get("date")).format("yyyy-MM-dd HH:mm:ss"))

            val searchRequest = SearchRequest().indices(INDICES_NAME)
                    .source(SearchSourceBuilder.searchSource().fetchSource(false).size(0).query(bq)
                            .aggregation(aggs))

            println(searchRequest.toString())

            val searchResponse = client.search(searchRequest)

            val terms: Terms = searchResponse.getAggregations().get("distinct")
            val buckets = terms.buckets

            searchPager.data = List(buckets.size - startIndex, { y ->
                var x = y + startIndex
                mapOf(
                        "userName" to buckets.get(x).keyAsString.split("  ")[0],
                        "userGroup" to buckets.get(x).keyAsString.split("  ")[1],
                        "loginCount" to buckets.get(x).docCount,
                        "firstLoginDate" to buckets.get(x).aggregations.get<Min>("_date_min_by").valueAsString,
                        "lastLoginDate" to buckets.get(x).aggregations.get<Max>("_date_max_by").valueAsString
                )
            })
        }
        return ApiResponse(searchPager, elapsed_time_)
    }

    override fun getUserLoginDetailsCount(condition: SearchCondition): ApiResponse {
        var searchPager = BasePage(condition.currentPage, condition.pageSize)
        var data: List<Map<String, Any>> = emptyList()
        val elapsed_time_ = measureTimeMillis {
            condition.exactList.plus(getExactCond())
            val _exactList = condition.exactList
            _exactList.plus(getExactCond())
            val qb = BaseDao.queryBuilders(_exactList, condition.dimList, condition.scopeList)

            val startIndex = (condition.currentPage - 1) * condition.pageSize
            val endIndex = condition.currentPage * condition.pageSize
            val searchRequest = SearchRequest().indices(INDICES_NAME).source(SearchSourceBuilder.searchSource()
                    .from(startIndex).size(endIndex).query(qb).sort("log_timestamp.keyword", SortOrder.DESC))

            val searchResponse = client.search(searchRequest)
            val searchHits = searchResponse.getHits().getHits()
            searchPager.totalHits = searchResponse.hits.totalHits
            searchPager.data = List<Map<String, Any?>>(searchHits.size, { x ->
                val s = searchHits.get(x).sourceAsMap
                mapOf(
                        "userId" to s.get(ModelContants.PARAMS_MAPS.get("userId")),
                        "userGroup" to s.get(ModelContants.PARAMS_MAPS.get("userGroup")),
                        "userName" to s.get(ModelContants.PARAMS_MAPS.get("userName")),
                        "userAuth" to s.get(ModelContants.PARAMS_MAPS.get("userAuth")),
                        "operation" to s.get(ModelContants.PARAMS_MAPS.get("operation")),
                        "macAddress" to s.get(ModelContants.PARAMS_MAPS.get("macAddress")),
                        "certificateServer" to s.get(ModelContants.PARAMS_MAPS.get("certificateServer")),
                        "linkInterface" to s.get(ModelContants.PARAMS_MAPS.get("linkInterface")),
                        "macAddress" to s.get(ModelContants.PARAMS_MAPS.get("macAddress")),
                        "deviceType" to s.get(ModelContants.PARAMS_MAPS.get("deviceType")),
                        "ipAddress" to s.get(ModelContants.PARAMS_MAPS.get("ipAddress")),
                        "logTimeStamp" to s.get(ModelContants.PARAMS_MAPS.get("logTimeStamp")),
                        "message" to s.get(ModelContants.PARAMS_MAPS.get("message")),
                        "logLevel" to s.get(ModelContants.PARAMS_MAPS.get("logLevel"))
                )
            })
        }
        return ApiResponse(searchPager, elapsed_time_)
    }

    fun getUserLoginTotalByUsername(rangeCondition: RangeCondition, userName: String): Long {
        val qCon3 = QueryCondition("user_name.keyword", userName, QueryCondition.QueryType.EXACT)
        return BaseDao.getListCountOfGroupBy(client, INDICES_NAME,
                SearchCondition(listOf(rangeCondition), getExactCond().plus(qCon3)),
                "doc['user_name.keyword'].values[0] + '  ' + doc['user_group.keyword'].values[0]")
    }

    fun getUserLoginTotal(rangeCondition: RangeCondition): Long {
        return BaseDao.getListCountOfGroupBy(client, INDICES_NAME, SearchCondition(listOf(rangeCondition), getExactCond()),
                "doc['user_name.keyword'].values[0] + '  ' + doc['user_group.keyword'].values[0]")
    }

    fun getUserTotal(rangeCondition: RangeCondition): Long {
        return BaseDao.getListCountOfGroupBy(client, INDICES_NAME, SearchCondition(listOf(rangeCondition), getExactCond()),
                "doc['user_name.keyword'].values[0]")
    }

    private fun getExactCond(): List<QueryCondition> {
        val qCon1 = QueryCondition("keyword_status", "SUCCESS", QueryCondition.QueryType.EXACT)
        val qCon2 = QueryCondition("operation.keyword", "LOGIN", QueryCondition.QueryType.EXACT)
        return listOf(qCon1, qCon2)
    }

    private fun getExactCond2(): Map<String, Array<String>> {
        return mapOf(
                "status" to arrayOf("SUCCESS"),
                "operation.keyword" to arrayOf("LOGIN")
        )
    }
}