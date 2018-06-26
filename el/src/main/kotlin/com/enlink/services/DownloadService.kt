package com.enlink.services

import com.enlink.config.properties.PathProps
import com.enlink.platform.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import java.util.Random
import java.util.concurrent.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService


/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/26 16:55
 * @description
 */
@Component
class DownloadService {
    val LOGGER: Logger = LoggerFactory.getLogger(DownloadService::class.java)
    @Autowired
    lateinit var pathProps: PathProps
    @Autowired
    lateinit var client: RestHighLevelClient

    fun downloadNew(conditions: List<DownloadCondition>) {
        conditions.forEach { x ->

        }
    }

    fun download(conditions: List<DownloadCondition>): Array<String> {
        val pathArrays = mutableListOf<String>()
        val futureTasks = mutableListOf<Future<String>>()
//        conditions.forEach { x ->
//            futureTasks.add(doSomething(x))
//        }
//        futureTasks.forEach { it ->
//            pathArrays.add(it.get())
//        }

        val threadPool = Executors.newFixedThreadPool(10)
        conditions.forEach { x ->
            val future = threadPool.submit(Callable {
                mkExcel(x)
            })
            futureTasks.add(future)
        }
        futureTasks.forEach { it ->
            pathArrays.add(it.get())
        }
        return pathArrays.toTypedArray()
    }

    fun mkExcel(x: DownloadCondition): String {
        val indexName = IndexMappings.Index_mappings.get(x.logType)!!
        val tempPath = this::class.java.classLoader.getResource("temps/${indexName}Template.xlsx").path
        var startIndex = 2
        val request = SearchRequest().indices(indexName)
                .source(SearchSourceBuilder()
                        .query(queryBuilders(x))
                        .size(200))
                .scroll(TimeValue(1, TimeUnit.MINUTES))
        LOGGER.info(request.source().toString())
        var resp = client.search(request)
        var total = resp.hits.hits.size
        if (total > 0) {
            val date = Date()
            val filePath = "${pathProps.tmp}/${date.date2string()}/${indexName}-${date.datetime2filename()}.xlsx"
            val df = File(filePath)
            if (!df.exists()) {
                File(tempPath).copyTo(df)
            }
            do {
                val searchHits = resp.getHits().getHits()
                val dateArrays = mutableListOf<Array<String>>()
                val headers = getHeaders(indexName)
                searchHits.forEach { sh ->
                    val map = sh.sourceAsMap
                    val dd = headers.map { h -> map.get(h).toString() }.toTypedArray()
                    dateArrays.add(dd)
                }
                ExcelUtils.genExcel(filePath, headers, dateArrays, startIndex)
                resp = client.searchScroll(SearchScrollRequest().scrollId(resp.scrollId).scroll(TimeValue(1, TimeUnit.MINUTES)))
                total = resp.hits.hits.size
                startIndex = startIndex + dateArrays.size
                println(startIndex)
            } while (total > 0)
            return filePath
        }
        return ""
    }

    fun queryBuilders(ds: DownloadCondition): BoolQueryBuilder {
        val bq = QueryBuilders.boolQuery()
        // 精确查询
        ds.preciseConditions?.forEach { it ->
            bq.filter(QueryBuilders.termsQuery(IndexMappings.Index_field_mappings[it.key], it.value.toMutableList()))
        }
        // 模糊查询
        ds.ambiguousConditions?.forEach { it ->
            bq.must(QueryBuilders.matchQuery(IndexMappings.Index_field_mappings[it.key], it.value))
        }
        // 范围查询
        ds.rangeConditionList?.forEach { it ->
            when (it.type) {
                RangeCondition.Type.DATE -> {
                    bq.must(QueryBuilders.rangeQuery(IndexMappings.Index_field_mappings[it.conditionName]).gte(it.gteValue).lte(it.lteValue)
                            .timeZone(it.timeZone))
                }
                else -> {
                    bq.must(QueryBuilders.rangeQuery(IndexMappings.Index_field_mappings[it.conditionName]).gte(it.gteValue).lte(it.lteValue))
                }
            }
        }
        return bq
    }

    fun getHeaders(index: String): Array<String> = when (index) {
        "user" -> {
            arrayOf(
                    "log_timestamp",
                    "user_name",
                    "user_group",
                    "keyword_user_auth",
                    "operation",
                    "keyword_status",
                    "ip_address",
                    "client_info",
                    "certificate_server",
                    "link_interface"
            )
        }
        "admin" -> {
            arrayOf(
                    "log_timestamp",
                    "user_name",
                    "user_group",
                    "keyword_user_auth",
                    "operation",
                    "log_info",
                    "ip_address",
                    "mac_address"
            )
        }
        "res" -> {
            arrayOf(
                    "log_timestamp",
                    "user_name",
                    "user_group",
                    "resource_name",
                    "uri",
                    "long_uplink_traffic",
                    "long_downlink_traffic",
                    "long_total_traffic",
                    "keyword_file_format",
                    "browser_info",
                    "url_http"
            )
        }
        "system" -> {
            arrayOf(
                    "log_timestamp",
                    "operate_type",
                    "cpu",
                    "memory",
                    "disk",
                    "fan",
                    "temperature"
            )
        }
        else -> {
            arrayOf(
                    "log_timestamp",
                    "user_name",
                    "user_group",
                    "keyword_user_auth",
                    "operation",
                    "keyword_status",
                    "ip_address",
                    "client_info",
                    "certificate_server",
                    "link_interface"
            )
        }
    }
}