package com.es.api

import com.es.common.*
import com.es.dao.BaseDao
import com.es.model.BasePage
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.elasticsearch.search.sort.SortOrder


/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/11 16:01
 * @description
 */
@RestController
@RequestMapping("/log")
class LogAction(val client: RestHighLevelClient) : BaseAction(client) {
    /**
     * @api {POST} api/log/log/list 用户日志查询/管理员日志查询/系统日志查询
     * @apiGroup log
     * @apiVersion 0.0.1
     * @apiDescription 用于查询用户日志/管理员日志/系统日志
     * @apiParam {Condition} condition 组合查询条件封装对象
     * @apiParam {String} logType 指定查询类别<span style="color:red">【可取值为userLog、adminLog、systemLog，分别对应用户日志/管理员日志/系统日志】</span>
     * @apiParamExample {String} 请求样例：
     * api/log/log/list?logType=userLog
     * @apiParamExample {json} 请求Body样例：
     * condition:
     * {
     * "preciseConditions": {
     * "key1":["val1","val2"],
     * "key2":["val1"]
     * },
     * "ambiguousConditions": {
     * "key1":"val1",
     * "key2":"val2"
     * },
     * "rangeConditionList": [
     * {
     * "type": "DATE",
     * "conditionName": "date1",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * },
     * {
     * "type": "DATE",
     * "conditionName": "date2",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * ],
     * "sortConditions": {
     * "key1":"ASC",
     * "key2":"DESC"
     * },
     * "currentPage": 1,
     * "pageSize": 10
     * }
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": [
     * {
     * "timeStamp": "2017-06-09T13:17:44.000Z",
     * "host": null,
     * "message": "INFO|UPDATE USERNAME|9/Jun/2017:21:17:44 +0800|131250195|XiaoXing|MingGroup|admin|192.168.5.111|AHS3FR466DF8DE|user name change from a to b|",
     * ...
     * },
     * ...,
     * ],
     * "extend": {
     * "page": {
     * "total": 101,
     * "totalPages": 11,
     * "pageSize": 10,
     * "currentPage": 1
     * }
     * }
     * }
     */
    @RequestMapping(value = "list", method = arrayOf(RequestMethod.POST))
    fun getLogList(@RequestBody condition: Condition, logType: String): ApiResponse {
        LOGGER.info(GsonUtils.convert(condition))
        val beginIndex = (condition.currentPage - 1) * condition.pageSize
        val pageSize = condition.pageSize
        val q = BaseDao.queryBuildersOld(condition.preciseConditions, condition.ambiguousConditions, condition.rangeConditionList)
        if (logType.equals("userLog")) {
            q.must(QueryBuilders.existsQuery("user_name"))
        }
        val total = client.search(SearchRequest(LogFields.LOG_INDEX_MAP.get(logType))
                .source(SearchSourceBuilder.searchSource().query(q).size(0))).hits.totalHits
        val page = BasePage(condition.currentPage, condition.pageSize, totalHits = total)
        if (total != 0L) {
            val _search = SearchRequest(LogFields.LOG_INDEX_MAP.get(logType)).source(
                    SearchSourceBuilder().query(q).sort("@timestamp", SortOrder.DESC).from(beginIndex).size(pageSize))
            return ApiResponse(super.covertSearchHit2Model(client.search(_search).hits.hits, logType), mapOf("page" to page))
        }
        return ApiResponse(null, mapOf("page" to page))
    }

    /**
     * @api {GET} api/log/log/groupConditions 获取下拉菜单列表中的值
     * @apiGroup log
     * @apiVersion 0.0.1
     * @apiDescription 用于获取下来菜单中的值
     * @apiParam {String} logType 指定查询日志类别<span style="color:red">【精确条件 ：xxx.keyword;模糊条件 : xxx】</span>
     * @apiParam {String} condition 指定下拉菜单条件名<span style="color:red">【可取值为userLog、adminLog、systemLog，分别对应用户日志/管理员日志/系统日志】</span>
     * @apiParamExample {String} 请求样例：
     * api/log/log/groupConditions?logType=userLog&condition=deviceType.keyword
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": [
     * "131250191",
     * "131250193",
     * "131250196"
     * ],
     * "extend": null
     * }
     */
    @RequestMapping(value = "/groupConditions", method = arrayOf(RequestMethod.GET))
    fun getGroupConditions(logType: String, condition: String): ApiResponse {
        val indexName = LogFields.LOG_INDEX_MAP.get(logType)!!
        val sr = super.searchRequestBuilder(logType)
        val agg = AggregationBuilders.terms("agg").field(LogFields.FIELD_MAP.get(condition)!!).size(Int.MAX_VALUE)
        sr.source(SearchSourceBuilder.searchSource().aggregation(agg))
        super.LOGGER.trace(sr.toString())
        val resp = client.search(sr)
        val terms = resp.getAggregations().get<Terms>("agg")
        val list = List<String>(terms.getBuckets().size, { x -> terms.getBuckets().get(x).keyAsString })
        return ApiResponse(list)
    }

    @RequestMapping(value = "/totalLoginCount", method = arrayOf(RequestMethod.POST))
    fun totalLoginCount(@RequestBody condition: RangeCondition): ApiResponse {
        val map = mapOf<String, Array<String>>(
                "status.keyword" to arrayOf("SUCCESS"),
                "operation.keyword" to arrayOf("LOGIN")
        )
        val qb = BaseDao.queryBuildersOld(map, emptyMap(), listOf(condition))
        val reque = SearchRequest().indices(LogFields.LOG_INDEX_MAP["userLog"])
                .source(SearchSourceBuilder().fetchSource(false).query(qb).size(0))
        return ApiResponse(client.search(reque).hits.totalHits)
    }

    @RequestMapping(value = "/userResFlow", method = arrayOf(RequestMethod.POST))
    fun userResFlow(@RequestBody condition: RangeCondition): ApiResponse {
        println(GsonUtils.convert(condition))
        val qb = BaseDao.queryBuildersOld(emptyMap(), emptyMap(), listOf(condition))
        val aggs = AggregationBuilders.sum("_key").field("long_total_traffic")
        val reque = SearchRequest().indices(LogFields.LOG_INDEX_MAP["resLog"])
                .source(SearchSourceBuilder().fetchSource(false).query(qb).size(0).aggregation(aggs))
        val total = client.search(reque).aggregations.get<Sum>("_key").valueAsString
        return ApiResponse(total)
    }
}