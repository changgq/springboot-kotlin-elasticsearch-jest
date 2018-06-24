package com.enlink.controller

import com.enlink.config.ElasticsearchProperties
import com.enlink.platform.CommonResponse
import com.enlink.platform.GsonUtils
import com.enlink.services.IndexService
import com.sun.org.apache.xpath.internal.operations.Bool
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.http.ConnectionClosedException
import org.apache.http.conn.ConnectTimeoutException
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.metrics.max.Max
import org.elasticsearch.search.aggregations.metrics.min.Min
import org.elasticsearch.search.builder.SearchSourceBuilder
import kotlin.system.measureTimeMillis
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHeader
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseException
import org.elasticsearch.client.ResponseListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.SSLHandshakeException


@RestController
@RequestMapping("/cluster")
class ClusterController : BaseController() {
    @Autowired
    lateinit var indexService: IndexService

    /**
     * @api {GET} /api/cluster/info 1_集群基本信息
     * @apiGroup cluster
     * @apiVersion 1.0.0
     * @apiDescription 集群基本信息
     * @apiSuccess {ApiResponse} apiResponse Json格式返回响应结果
     * @apiSuccessExample {json} 返回样例:
     * {"data":[{"clusterName":"eslogs","clusterUuid":"03VfExIgRbGSp5tcW8jsxQ","nodeName":"master","version":"6.2.4","buildTime":"2018-04-12T20:37:28.497551Z"}],"response_time":71,"status_code":200,"message":"OK"}
     */
    @GetMapping("/info")
    fun info(): CommonResponse {
        var data = emptyMap<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val info = client.info()
            data = mapOf(
                    "clusterName" to info.clusterName.value(),
                    "clusterUuid" to info.clusterUuid,
                    "nodeName" to info.nodeName,
                    "version" to info.version.toString(),
                    "buildTime" to info.build.date()
            )
        }
        LOGGER.info(GsonUtils.convert(data))
        return CommonResponse(data, elapsed_time_)
    }

    @GetMapping("/getIndexMinDate")
    fun getIndexMinDate(indexName: String): String {
        val _request = SearchRequest(indexName)
                .source(SearchSourceBuilder()
                        .fetchSource(false)
                        .aggregation(AggregationBuilders
                                .min("min")
                                .field("@timestamp")
                                .format("yyyy-MM-dd")))
        LOGGER.info(_request.source().toString())
        return client.search(_request).aggregations.get<Min>("min").valueAsString
    }

    @GetMapping("/getIndexMaxDate")
    fun getIndexMaxDate(indexName: String): String {
        val _request = SearchRequest(indexName)
                .source(SearchSourceBuilder()
                        .fetchSource(false)
                        .aggregation(AggregationBuilders
                                .max("max")
                                .field("@timestamp")
                                .format("yyyy-MM-dd")))
        LOGGER.info(_request.source().toString())
        return client.search(_request).aggregations.get<Max>("max").valueAsString
    }

    @PostMapping("/reindex")
    fun reindex(@RequestBody jsons: String): CommonResponse {
        return CommonResponse(indexService.reindex(jsons), 0)
    }

    @GetMapping("/existsIndex")
    fun existsIndex(indices: String): CommonResponse {
        var resp: Boolean = false
        val response_time = measureTimeMillis {
            resp = indexService.existsIndex(indices)
        }
        return CommonResponse(resp, response_time)
    }

    @GetMapping("/deleteIndex")
    fun deleteIndex(indices: String): CommonResponse {
        var resp: Boolean = false
        val response_time = measureTimeMillis {
            resp = indexService.deleteIndex(indices)
        }
        return CommonResponse(resp, response_time)
    }

    @GetMapping("/openIndex")
    fun openIndex(indices: String): CommonResponse {
        var resp: Boolean = false
        val response_time = measureTimeMillis {
            resp = indexService.openIndex(indices)
        }
        return CommonResponse(resp, response_time)
    }

    @GetMapping("/closeIndex")
    fun closeIndex(indices: String): CommonResponse {
        var resp: Boolean = false
        val response_time = measureTimeMillis {
            resp = indexService.closeIndex(indices)
        }
        return CommonResponse(resp, response_time)
    }
}