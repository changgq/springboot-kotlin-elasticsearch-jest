package com.es.services.impl

import com.es.common.ApiResponse
import com.es.common.QueryCondition
import com.es.common.RangeCondition
import com.es.common.SearchCondition
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
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.metrics.max.Max
import org.elasticsearch.search.aggregations.metrics.min.Min
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
class UserLogServiceImpl(val highLevelClient: RestHighLevelClient) : UserLogService {
    val INDICES_NAME = "user"

    override fun getUserLoginRank(rangeCondition: RangeCondition, topCount: Int): ApiResponse {
        var echartsData = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val searchCond = SearchCondition(listOf(rangeCondition), getExactCond())
            val buckets = BaseDao.getListOfGroupBy(highLevelClient, INDICES_NAME, searchCond, "doc['user_name.keyword'].values[0]", topCount, false)
            echartsData = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString }),
                    yAxis_Integer = Array<Int>(buckets.size, { x -> buckets.get(x).docCount.toInt() }))
        }
        return ApiResponse(echartsData, elapsed_time_)
    }

    override fun getUserLoginRankAndFlow(rangeCondition: RangeCondition, topCount: Int): ApiResponse {
        val apiResponse = getUserLoginRank(rangeCondition, topCount)
        var echartsData = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val ue: EchartsData = apiResponse.data as EchartsData
            val yAxisString = Array<String>(ue.xAxis_String.size, { it -> it.toString() })
            var index = 0
            ue.xAxis_String.forEach { it ->
                val qCon = QueryCondition("user_name.keyword", it, QueryCondition.QueryType.EXACT)
                val searchCond = SearchCondition(listOf(rangeCondition), listOf(qCon))
                val sumValue = BaseDao.getValueBySum(highLevelClient, "res", searchCond, "doc['long_total_traffic'].value")
                yAxisString.set(index++, sumValue.toString())
            }
            // 封装图表数据对象
            echartsData = EchartsData(xAxis_String = ue.xAxis_String, yAxis_Integer = ue.yAxis_Integer, yAxis_String = yAxisString)
        }
        return ApiResponse(echartsData, elapsed_time_)
    }

    override fun getTerminalPie(rangeCondition: RangeCondition): ApiResponse {
        var echartsData = EchartsData()
        val elapsed_time_ = measureTimeMillis {
            val searchCond = SearchCondition(listOf(rangeCondition), getExactCond())
            val buckets = BaseDao.getListOfGroupBy(highLevelClient, INDICES_NAME, searchCond, "doc['device_os.keyword'].values[0]", Int.MAX_VALUE)
            // 封装图表数据对象
            echartsData = EchartsData(xAxis_String = Array<String>(buckets.size, { x -> buckets.get(x).keyAsString }),
                    yAxis_Integer = Array<Int>(buckets.size, { x -> buckets.get(x).docCount.toInt() }))
        }
        return ApiResponse(echartsData, elapsed_time_)
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

            val searchResponse = highLevelClient.search(searchRequest)
            loginCount = searchResponse.hits.totalHits
        }
        return ApiResponse(loginCount, elapsed_time_)
    }

    override fun getUserLoginGroupStatics(condition: SearchCondition): ApiResponse {
        val total = getUserLoginTotal(condition.rangeQueryCons!!.get(0))
        var searchPager = BasePage(condition.currentPage, condition.pageSize, "", total)
        var loginCount = 0L
        var data: List<Map<String, Any>> = emptyList()
        val elapsed_time_ = measureTimeMillis {
            val bq = BaseDao.queryBuilders(getExactCond(), condition.dimList, condition.scopeList)
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

            val searchResponse = highLevelClient.search(searchRequest)

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

            val searchResponse = highLevelClient.search(searchRequest)
            val searchHits = searchResponse.getHits().getHits()
            searchPager.total = searchResponse.hits.totalHits
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
        return BaseDao.getListCountOfGroupBy(highLevelClient, INDICES_NAME,
                SearchCondition(listOf(rangeCondition), getExactCond().plus(qCon3)),
                "doc['user_name.keyword'].values[0] + '  ' + doc['user_group.keyword'].values[0]")
    }

    fun getUserLoginTotal(rangeCondition: RangeCondition): Long {
        return BaseDao.getListCountOfGroupBy(highLevelClient, INDICES_NAME, SearchCondition(listOf(rangeCondition), getExactCond()),
                "doc['user_name.keyword'].values[0] + '  ' + doc['user_group.keyword'].values[0]")
    }

    fun getUserTotal(rangeCondition: RangeCondition): Long {
        return BaseDao.getListCountOfGroupBy(highLevelClient, INDICES_NAME, SearchCondition(listOf(rangeCondition), getExactCond()),
                "doc['user_name.keyword'].values[0]")
    }

    private fun getExactCond(): List<QueryCondition> {
        val qCon1 = QueryCondition("keyword_status", "SUCCESS", QueryCondition.QueryType.EXACT)
        val qCon2 = QueryCondition("operation.keyword", "LOGIN", QueryCondition.QueryType.EXACT)
        return listOf(qCon1, qCon2)
    }
}