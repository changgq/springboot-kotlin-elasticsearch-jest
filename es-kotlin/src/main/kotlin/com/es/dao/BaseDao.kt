package com.es.dao

import com.es.common.*
import com.es.model.ModelContants
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.BucketOrder
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 功能描述：基础Dao
 *
 * @auther changgq
 * @date 2018/5/31 13:32
 * @description
 */
object BaseDao {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    val MAX_INT_VALUE = 10000


    fun queryBuildersOld(exactMap: Map<String, Array<String>> = emptyMap(),
                         dimMap: Map<String, String> = emptyMap(),
                         scopeList: List<RangeCondition> = emptyList()): BoolQueryBuilder {
        LOGGER.info("exactList = ${GsonUtils.convert(exactMap)}, dimList = ${GsonUtils.convert(dimMap)}, scopeList = ${GsonUtils.convert(scopeList)}")
        val bq = QueryBuilders.boolQuery()
        // 精确查询
        exactMap?.forEach { it ->
            bq.filter(QueryBuilders.termsQuery(LogFields.FIELD_MAP[it.key], it.value.asList()))
        }
        // 模糊查询
        dimMap?.forEach { it ->
            bq.must(QueryBuilders.matchQuery(LogFields.FIELD_MAP[it.key], it.value))
        }
        // 范围查询
        scopeList?.forEach { it ->
            when (it.type) {
                RangeCondition.Type.DATE -> {
                    bq.must(QueryBuilders.rangeQuery(LogFields.FIELD_MAP[it.conditionName])
                            .gte(it.gteValue).lte(it.lteValue).timeZone(it.timeZone))
                }
                else -> {
                    bq.must(QueryBuilders.rangeQuery(LogFields.FIELD_MAP[it.conditionName])
                            .gte(it.gteValue).lte(it.lteValue))
                }
            }
        }
        return bq
    }


    fun queryBuilders(exactList: List<QueryCondition> = emptyList(),
                      dimList: List<QueryCondition> = emptyList(),
                      scopeList: List<RangeCondition> = emptyList()): BoolQueryBuilder {
        LOGGER.info("exactList = ${GsonUtils.convert(exactList)}, dimList = ${GsonUtils.convert(dimList)}, scopeList = ${GsonUtils.convert(scopeList)}")
        val bq = QueryBuilders.boolQuery()
        // 精确查询
        exactList?.forEach { it ->
            bq.must(QueryBuilders.termsQuery(it.queryName, it.queryValue))
        }
        // 模糊查询
        dimList?.forEach { it ->
            bq.must(QueryBuilders.matchQuery(it.queryName, it.queryValue))
        }
        // 范围查询
        scopeList?.forEach { it ->
            when (it.type) {
                RangeCondition.Type.DATE -> {
                    bq.must(QueryBuilders.rangeQuery(it.conditionName)
                            .gte(it.gteValue).lte(it.lteValue).timeZone(it.timeZone))
                }
                else -> {
                    bq.must(QueryBuilders.rangeQuery(it.conditionName)
                            .gte(it.gteValue).lte(it.lteValue))
                }
            }
        }
        return bq
    }

    fun aggBuilders(groupBy: String, topCount: Int, sortIsAsc: Boolean, absQueryCons: Map<String, String>?): TermsAggregationBuilder {
        val ab = AggregationBuilders.terms("_groupBy")
                .field(ModelContants.PARAMS_MAPS.get(groupBy))
                .size(if (topCount == 0) MAX_INT_VALUE else topCount)
        absQueryCons?.forEach { m ->
            val aggName = "_" + m.value + "_by"
            when (m.value) {
                "max" -> ab.subAggregation(AggregationBuilders.max(aggName).field(ModelContants.PARAMS_MAPS.get(m.key)))
                "min" -> ab.subAggregation(AggregationBuilders.max(aggName).field(ModelContants.PARAMS_MAPS.get(m.key)))
                "count" -> ab.subAggregation(AggregationBuilders.count(aggName).field(ModelContants.PARAMS_MAPS.get(m.key)))
                "sum" -> ab.subAggregation(AggregationBuilders.sum(aggName).field(ModelContants.PARAMS_MAPS.get(m.key)))
                "avg" -> ab.subAggregation(AggregationBuilders.avg(aggName).field(ModelContants.PARAMS_MAPS.get(m.key)))
                "subAggs" -> ab.subAggregation(AggregationBuilders.terms("_" + m.key + "_groupBy").field(ModelContants.PARAMS_MAPS.get(m.key)))
            }
        }
        return ab
    }


    fun getListTotal(highLevelClient: RestHighLevelClient, indicesName: String, sc: SearchCondition): Long {
        return highLevelClient.search(createSearchRequest(indicesName, sc, null)).hits.totalHits
    }

    /**
     * 功能描述: 查询获取记录数量
     * @auther changgq
     * @date 2018/6/5 14:17
     *
     * @param highLevelClient
     * @param indicesName
     * @param sc
     * @param scripts 示例：doc['resource_name.keyword'].value
     * @return
     */
    fun getListCountOfGroupBy(highLevelClient: RestHighLevelClient, indicesName: String, sc: SearchCondition, scripts: String): Long {
        val aggs = AggregationBuilders.cardinality("distinct").script(Script(scripts))
        return highLevelClient.search(createSearchRequest(indicesName, sc, aggs)).aggregations.get<Cardinality>("distinct").value
    }

    fun getValueBySum(highLevelClient: RestHighLevelClient, indicesName: String, sc: SearchCondition, scripts: String): Double {
        val aggs = AggregationBuilders.sum("sum_value").script(Script(scripts))
        return highLevelClient.search(createSearchRequest(indicesName, sc, aggs)).aggregations.get<Sum>("sum_value").value
    }

    fun getListOfGroupBy(highLevelClient: RestHighLevelClient, indicesName: String, sc: SearchCondition, scripts: String, topCount: Int = 0, termSort: Boolean = false): List<Terms.Bucket> {
        val size = if (topCount == 0) sc.currentPage * sc.pageSize else topCount
        val aggs = AggregationBuilders.terms("_key").script(Script(scripts)).size(size)
        return highLevelClient.search(createSearchRequest(indicesName, sc, aggs)).aggregations.get<Terms>("_key").buckets
    }

    fun createSearchRequest(indicesName: String, sc: SearchCondition?, aggs: AggregationBuilder?): SearchRequest {
        val searchSource = SearchSourceBuilder().fetchSource(false).size(0)
        if (null != sc) {
            searchSource.query(queryBuilders(sc.exactList, sc.dimList, sc.scopeList))
        }
        if (null != aggs) {
            searchSource.aggregation(aggs)
        }
        val searchRequest = SearchRequest().indices(indicesName).searchType(SearchType.QUERY_THEN_FETCH).source(searchSource)
        LOGGER.info(searchRequest.toString())
        return searchRequest
    }
}