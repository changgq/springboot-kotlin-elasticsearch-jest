package com.enlink.controller

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.enlink.config.properties.PathProps
import com.enlink.dao.DocumentDao
import com.enlink.model.LogSetting
import com.enlink.platform.*
import com.enlink.platform.GsonUtils.reConvert
import com.enlink.services.DownloadService
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType
import okhttp3.Request
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.BucketOrder
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.metrics.max.Max
import org.elasticsearch.search.aggregations.metrics.min.Min
import org.elasticsearch.search.aggregations.metrics.sum.Sum
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder
import org.joda.time.DateTimeZone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.math.BigDecimal
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletResponse
import kotlin.streams.toList
import kotlin.system.measureTimeMillis

/**
 * 功能描述：日志基础接口
 */
@RestController
@RequestMapping("/log")
class LogController : BaseController() {
    @Autowired
    lateinit var pathProps: PathProps
    @Autowired
    lateinit var downloadServie: DownloadService

    /**
     * 功能描述: 分页查询日志数据
     */
    @RequestMapping(value = "list", method = arrayOf(RequestMethod.POST))
    fun getLogList(@RequestBody condition: Condition, logType: String): CommonResponse {
        return list(condition, logType)
    }

    /**
     * 功能描述: 获取下拉菜单列表中的值
     */
    @RequestMapping(value = "/groupConditions", method = arrayOf(RequestMethod.GET))
    fun getGroupConditions(logType: String, condition: String): CommonResponse {
        var _request = SearchRequest(getIndexName(logType)).source(
                SearchSourceBuilder().fetchSource(false).size(0).aggregation(
                        AggregationBuilders.terms("distinct").field(getFieldName(condition)).size(MAX_AGGR_COUNT)))
        LOGGER.info(_request.toString())
        val resp = client.search(_request)
        val terms = resp.getAggregations().get<Terms>("distinct")
        val list = List<String>(terms.getBuckets().size, { x -> terms.getBuckets().get(x).keyAsString })
        return CommonResponse(list)
    }

    @RequestMapping(value = "/zip", method = arrayOf(RequestMethod.GET))
    fun zip(@RequestParam("match") match: String, response: HttpServletResponse) {
        try {
            LOGGER.info(" ------------->> $match")
            val matchType = object : TypeToken<List<DownloadCondition>>() {}.type
            val conditions = GsonBuilder().create()
                    .fromJson<List<DownloadCondition>>(URLDecoder.decode(match, "utf-8"), matchType)
            // 获取Excel文件
            val filePaths: Array<String> = downloadServie.downloadNew(conditions)
            LOGGER.info(GsonUtils.convert(filePaths))
            val zipPath = "${pathProps.tmp}日志下载内容_${Date().datetime2filename()}.zip"
            LOGGER.info("generate zipPath[$zipPath]")

            // 生成Zip文件
            ZipCompressor(zipPath).compress(*filePaths)

            // 文件下载
            val zipf = File(zipPath)
            val fis = BufferedInputStream(zipf.inputStream())
            val buffer = ByteArray(fis.available())
            fis.read(buffer)
            fis.close()
            val bufferOut = BufferedOutputStream(response.outputStream)
            response.contentType = "application/octet-stream"
            response.setHeader("Content-Disposition", "attachment;filename=${String(zipf.name.toByteArray(Charset.defaultCharset()), Charset.forName("ISO-8859-1"))}")
            bufferOut.write(buffer)
            bufferOut.flush()
            bufferOut.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 功能描述: 用户登录次数top5
     */
    @RequestMapping(value = "/loginTopCount", method = arrayOf(RequestMethod.POST))
    fun loginTopCount(@RequestBody rangeCondition: RangeCondition, topCount: Int = 5): CommonResponse {
        var dataArrays = mutableListOf<Map<String, Any>>()
        val expose_time = measureTimeMillis {
            val aggs = AggregationBuilders
                    .terms("login_count")
                    .field("user_name.keyword")
                    .size(topCount)
            val response = userRangeSearch(rangeCondition, "", aggs)
            val buckets = response.aggregations.get<Terms>("login_count").buckets
            buckets.forEach {

                val userName = it.keyAsString
                // 根据用户名查询用户总流量
                val aggs = AggregationBuilders
                        .sum("sum_total_traffic")
                        .field("long_total_traffic")
                val resp = resFlowResponse(rangeCondition, userName, aggs)
                val flow = resp.aggregations.get<Sum>("sum_total_traffic").value

                dataArrays.add(mapOf<String, Any>(
                        "name" to userName,
                        "count" to it.docCount,
                        "flow" to flow
                ))
            }
        }
        return CommonResponse(dataArrays, expose_time)
    }

    /**
     * 功能描述: 用户流量top5
     */
    @RequestMapping(value = "/trafficTopCount", method = arrayOf(RequestMethod.POST))
    fun trafficTopCount(@RequestBody rangeCondition: RangeCondition, topCount: Int = 5): CommonResponse {
        var dataArrays = mutableListOf<Map<String, Any>>()
        val expose_time = measureTimeMillis {
            val aggs = AggregationBuilders
                    .terms("user_names")
                    .field("user_name.keyword")
                    .subAggregation(AggregationBuilders.sum("total_traffic")
                            .field("long_total_traffic"))
                    .size(topCount)
                    .order(BucketOrder.aggregation("total_traffic", false))
            val resp = resFlowResponse(rangeCondition, "", aggs)
            val buckets = resp.aggregations.get<Terms>("user_names").buckets
            buckets.forEach {
                // 根据用户名获取用户登录次数
                val userName = it.keyAsString
                val aggs = AggregationBuilders
                        .count("login_count")
                        .field("user_name.keyword")
                val response = userRangeSearch(rangeCondition, userName, aggs)
                val loginCount = response.aggregations.get<ValueCount>("login_count").value

                dataArrays.add(mapOf<String, Any>(
                        "name" to userName,
                        "count" to loginCount,
                        "flow" to it.aggregations.get<Sum>("total_traffic").value
                ))
            }


        }
        return CommonResponse(dataArrays, expose_time)
    }

    /**
     * 功能描述：用户登录总次数
     */
    @RequestMapping(value = "/totalLoginCount", method = arrayOf(RequestMethod.POST))
    fun totalLoginCount(@RequestBody rangeCondition: RangeCondition): CommonResponse {
        var loginCount = 0L
        val expose_time = measureTimeMillis {
            val aggs = AggregationBuilders
                    .count("login_count")
                    .field("user_name.keyword")
            val response = userRangeSearch(rangeCondition, "", aggs)
            loginCount = response.aggregations.get<ValueCount>("login_count").value
        }
        return CommonResponse(loginCount, expose_time)
    }

    fun userRangeSearch(rangeCondition: RangeCondition, userName: String, aggs: AggregationBuilder): SearchResponse {
        val qb = QueryBuilders.boolQuery()
        if (userName.isNotBlank()) {
            qb.must(QueryBuilders.matchQuery("user_name.keyword", userName))
        }
        qb.must(QueryBuilders.matchQuery("operation", "LOGIN"))
        qb.must(QueryBuilders.matchQuery("keyword_status", "SUCCESS"))
        qb.must(QueryBuilders.rangeQuery("@timestamp")
                .gt(rangeCondition.gteValue)
                .lte(rangeCondition.lteValue)
                .timeZone(rangeCondition.timeZone))
        val source = SearchSourceBuilder()
                .size(0)
                .fetchSource(false)
                .query(qb)
                .aggregation(aggs)
        val request = SearchRequest()
                .indices(IndexMappings.Index_mappings.get("userLog"))
                .source(source)
        LOGGER.info(request.source().toString())
        return client.search(request)
    }

    /**
     * 功能描述: 用户总流量/应用总流量
     */
    @RequestMapping(value = "/resTotalFlow", method = arrayOf(RequestMethod.POST))
    fun resTotalFlow(@RequestBody rangeCondition: RangeCondition): CommonResponse {
        var totalFlow = 0.0
        val expose_time = measureTimeMillis {
            totalFlow = userResFlowSearch(rangeCondition, "")
        }
        return CommonResponse(totalFlow, expose_time)
    }

    /**
     * 功能描述: 资源访问量top5
     */
    @RequestMapping(value = "/resAccessTotalTopCount", method = arrayOf(RequestMethod.POST))
    private fun resAccessTotalTopCount(@RequestBody rangeCondition: RangeCondition, topCount: Int = 5): CommonResponse {
        var dataArrays = mutableListOf<Map<String, Any>>()
        val expose_time = measureTimeMillis {
            val aggs = AggregationBuilders
                    .terms("res_total")
                    .field("resource_name.keyword")
                    .size(topCount)
            val response = resFlowResponse(rangeCondition, "", aggs)
            val buckets = response.aggregations.get<Terms>("res_total").buckets
            buckets.forEach {
                dataArrays.add(mapOf(
                        "name" to it.keyAsString,
                        "value" to it.docCount
                ))
            }
        }
        return CommonResponse(dataArrays, expose_time)
    }

    /**
     * 功能描述: 资源流量top5
     */
    @RequestMapping(value = "/resTotalTopCount", method = arrayOf(RequestMethod.POST))
    private fun resTotalTopCount(@RequestBody rangeCondition: RangeCondition, topCount: Int = 5): CommonResponse {
        var dataArrays = mutableListOf<Map<String, Any>>()
        val expose_time = measureTimeMillis {
            val aggs = AggregationBuilders
                    .terms("res_flow_total")
                    .field("resource_name.keyword")
                    .size(topCount)
                    .subAggregation(AggregationBuilders
                            .sum("flow_total")
                            .field("long_total_traffic"))
                    .order(BucketOrder.aggregation("flow_total", false))
            val response = resFlowResponse(rangeCondition, "", aggs)
            val buckets = response.aggregations.get<Terms>("res_flow_total").buckets
            buckets.forEach {
                dataArrays.add(mapOf(
                        "name" to it.keyAsString,
                        "value" to it.aggregations.get<Sum>("flow_total").value
                ))
            }
        }
        return CommonResponse(dataArrays, expose_time)
    }

    /**
     * 功能描述: 根据年、月、日，或者时间范围获取资源总数
     */
    @RequestMapping(value = "/resTotal", method = arrayOf(RequestMethod.POST))
    private fun resTotal(@RequestBody rangeCondition: RangeCondition): CommonResponse {
        var data = 0L
        val expose_time = measureTimeMillis {
            val aggs = AggregationBuilders
                    .count("res_total")
                    .field("resource_name.keyword")
            val response = resFlowResponse(rangeCondition, "", aggs)
            data = response.aggregations.get<ValueCount>("res_total").value
        }
        return CommonResponse(data, expose_time)
    }

    fun userResFlowSearch(rangeCondition: RangeCondition, userName: String): Double {
        val aggs = AggregationBuilders
                .sum("user_flow_total")
                .field("long_total_traffic")
        val response = resFlowResponse(rangeCondition, "", aggs)
        return response.aggregations.get<Sum>("user_flow_total").value
    }


    fun resFlowResponse(rangeCondition: RangeCondition, userName: String, aggs: AggregationBuilder): SearchResponse {
        val qb = QueryBuilders.boolQuery()
        if (userName.isNotBlank()) {
            qb.must(QueryBuilders.matchQuery("user_name", userName))
        }
        qb.must(QueryBuilders.rangeQuery("@timestamp")
                .gt(rangeCondition.gteValue)
                .lte(rangeCondition.lteValue)
                .timeZone(rangeCondition.timeZone))
        val source = SearchSourceBuilder()
                .size(0)
                .fetchSource(false)
                .query(qb)
                .aggregation(aggs)
        val request = SearchRequest()
                .indices(IndexMappings.Index_mappings.get("resLog"))
                .source(source)
        LOGGER.info(request.source().toString())
        return client.search(request)
    }


    /**
     * 功能描述: 获取当天的网卡流量图
     */
    @RequestMapping(value = "/getNetworkTraffic", method = arrayOf(RequestMethod.GET, RequestMethod.POST))
    fun getNetworkTraffic(): CommonResponse {
        val inBytes = getNetworkInBytes()
        val outBytes = getNetworkOutBytes()
        val rm = mutableMapOf<String, Any>()
        rm.put("xAxis", inBytes.keys)
        rm.put("yAxisIn", inBytes.values)
        rm.put("yAxisOut", outBytes.values)
        return CommonResponse(rm)
    }

    fun getNetworkInBytes(): MutableMap<String, Any> {
        val _ss_ = mapOf<String, Any>(
                "size" to 0,
                "aggs" to mapOf<String, Any>(
                        "range" to mapOf<String, Any>(
                                "date_range" to mapOf(
                                        "field" to "@timestamp",
                                        "format" to "yyyy-MM-dd HH:mm:ss",
                                        "ranges" to listOf(
                                                mapOf(
                                                        "from" to "${Date().date2string()} 00:00:00",
                                                        "to" to "${Date().date2string()} 23:59:59"
                                                )
                                        ),
                                        "time_zone" to "+08:00"
                                ),
                                "aggs" to mapOf(
                                        "network_speed_per_day" to mapOf(
                                                "date_histogram" to mapOf(
                                                        "field" to "@timestamp",
                                                        "interval" to "10m"
                                                ),
                                                "aggs" to mapOf(
                                                        "network_bytes" to mapOf(
                                                                "max" to mapOf(
                                                                        "field" to "system.network.in.bytes"
                                                                )
                                                        ),
                                                        "network_bytes_deriv" to mapOf(
                                                                "derivative" to mapOf(
                                                                        "buckets_path" to "network_bytes",
                                                                        "unit" to "1s"
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        )
        val res: okhttp3.Response = okHttpClient.newCall(Request.Builder()
                .header("Content-Type", "application/json")
                .url("${pros.uris[0]}/metricbeat-*/_search")
                .post(okhttp3.RequestBody.create(MediaType.parse("application/json"), GsonUtils.convert(_ss_)))
                .build()).execute()
        res.header("Content-Type", "application/json")
        val dataMap = mutableMapOf<String, Any>()
        if (res.isSuccessful) {
            val responseBody = res.body()
            if (responseBody != null) {
                val result = responseBody.source().readString(Charset.defaultCharset())
                val jo: JSONObject = JSON.parseObject(result)
                val jo_buckets: JSONArray = jo.getJSONObject("aggregations").getJSONObject("range").getJSONArray("buckets")
                if (jo_buckets.size > 0) {
                    val jo_buckets_childs: JSONArray = jo_buckets.getJSONObject(0).getJSONObject("network_speed_per_day").getJSONArray("buckets")
                    for (joIndex in 0..jo_buckets_childs.size - 1) {
                        val joo: JSONObject = jo_buckets_childs.getJSONObject(joIndex)
                        val xAxis = Date(joo.getLong("key")).datetime2string()
                        val yAxis = if (joo.contains("network_bytes_deriv")) {
                            joo.getJSONObject("network_bytes_deriv").getDouble("normalized_value")
                        } else {
                            0.0
                        }
                        // 将流量单位显示为KB，并格式化保留2位小数
                        val y = yAxis.toBigDecimal().divide(BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP)
                        dataMap.put(xAxis, y.toDouble())
                    }
                }
                responseBody.close()
            }
        }
        return dataMap
    }

    fun getNetworkOutBytes(): MutableMap<String, Any> {
        val _ss_ = mapOf<String, Any>(
                "size" to 0,
                "aggs" to mapOf<String, Any>(
                        "range" to mapOf<String, Any>(
                                "date_range" to mapOf(
                                        "field" to "@timestamp",
                                        "format" to "yyyy-MM-dd HH:mm:ss",
                                        "ranges" to listOf(
                                                mapOf(
                                                        "from" to "${Date().date2string()} 00:00:00",
                                                        "to" to "${Date().date2string()} 23:59:59"
                                                )
                                        ),
                                        "time_zone" to "+08:00"
                                ),
                                "aggs" to mapOf(
                                        "network_speed_per_day" to mapOf(
                                                "date_histogram" to mapOf(
                                                        "field" to "@timestamp",
                                                        "interval" to "10m"
                                                ),
                                                "aggs" to mapOf(
                                                        "network_bytes" to mapOf(
                                                                "max" to mapOf(
                                                                        "field" to "system.network.out.bytes"
                                                                )
                                                        ),
                                                        "network_bytes_deriv" to mapOf(
                                                                "derivative" to mapOf(
                                                                        "buckets_path" to "network_bytes",
                                                                        "unit" to "1s"
                                                                )
                                                        )
                                                        ,
                                                        "network_bytes_deriv_negative" to mapOf(
                                                                "bucket_script" to mapOf(
                                                                        "buckets_path" to mapOf(
                                                                                "rate" to "network_bytes_deriv.normalized_value"
                                                                        ),
                                                                        "script" to "params.rate != null && params.rate > 0 ? params.rate * -1 : null"
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        )
        val dataMap = mutableMapOf<String, Any>()
        val res: okhttp3.Response = okHttpClient.newCall(Request.Builder()
                .header("Content-Type", "application/json")
                .url("${pros.uris[0]}/metricbeat-*/_search")
                .post(okhttp3.RequestBody.create(MediaType.parse("application/json"), GsonUtils.convert(_ss_)))
                .build()).execute()
        res.header("Content-Type", "application/json")
        if (res.isSuccessful) {
            val responseBody = res.body()
            if (responseBody != null) {
                val result = responseBody.source().readString(Charset.defaultCharset())
                val jo: JSONObject = JSON.parseObject(result)
                val jo_buckets: JSONArray = jo.getJSONObject("aggregations").getJSONObject("range").getJSONArray("buckets")
                if (jo_buckets.size > 0) {
                    val jo_buckets_childs: JSONArray = jo_buckets.getJSONObject(0).getJSONObject("network_speed_per_day").getJSONArray("buckets")
                    for (joIndex in 0..jo_buckets_childs.size - 1) {
                        val joo: JSONObject = jo_buckets_childs.getJSONObject(joIndex)
                        val xAxis = Date(joo.getLong("key")).datetime2string()
                        val yAxis = if (joo.contains("network_bytes_deriv_negative")) {
                            joo.getJSONObject("network_bytes_deriv_negative").getDouble("value")
                        } else {
                            0.0
                        }
                        // 将流量单位显示为KB，并格式化保留2位小数
                        val y = yAxis.toBigDecimal().divide(BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP)
                        dataMap.put(xAxis, y.toDouble())
                    }
                }
                responseBody.close()
            }
        }
        return dataMap
    }
}

/**
 * 功能描述：日志设置接口
 */
@RestController
@RequestMapping("/logconfig")
class LogConfigController() : BaseController() {
    @Autowired
    lateinit var documentDao: DocumentDao

    /**
     * 功能描述: 更新日志设置信息
     * @auther changgq
     * @date 2018/6/8 10:26
     *
     * @param
     * @return
     */
    @RequestMapping("/set", method = arrayOf(RequestMethod.POST))
    fun updateSetting(@RequestBody logSetting: LogSetting): CommonResponse {
        val rel = documentDao.update(".log-setting", logSetting.jsonString())
        return CommonResponse(rel == 200)
    }

    /**
     * 功能描述: 获取日志设置信息
     * @auther changgq
     * @date 2018/6/8 10:25
     *
     * @param
     * @return
     */
    @GetMapping("/get")
    fun get(): CommonResponse {
        val resp = documentDao.get(".log-setting")
        return CommonResponse(reConvert(resp, LogSetting::class.java))
    }
}

@RestController
@RequestMapping("/reslog")
class ResourceLogController : BaseController() {
    private val indicesName = IndexMappings.Index_mappings.get("resLog")

    // 完成 【资源统计与分析-访问资源排行-访问应用类型占比】
    @RequestMapping(value = "/applicationPie", method = arrayOf(RequestMethod.POST))
    fun applicationPie(@RequestBody rangeCondition: RangeCondition, topCount: Int): CommonResponse {
        var dataMap = mutableMapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(emptyMap(), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("appTypes")
                    .script(Script("doc['app_type.keyword'].value")).size(topCount)
                    .subAggregation(AggregationBuilders.count("appTypeCount").field("app_type.keyword"))
                    .order(BucketOrder.aggregation("appTypeCount", false))
            val request = SearchRequest().indices(indicesName)
                    .source(SearchSourceBuilder()
                            .fetchSource(false)
                            .size(0)
                            .query(qb)
                            .aggregation(aggs)
                            .timeout(TimeValue(2, TimeUnit.MINUTES)))
            LOGGER.info(request.toString())
            val buckets = client.search(request).aggregations.get<Terms>("appTypes").buckets
            val _total = buckets.map { it.docCount.toInt() }.sum()
            val totalTraffic = totalResStatistics(rangeCondition)
            val m = buckets.map { it.keyAsString to it.docCount.toInt() }.toMap()
            dataMap.putAll(m)
            dataMap.put("others", totalTraffic - _total)
        }
        return CommonResponse(dataMap, elapsed_time_)
    }

    // 完成 【资源统计与分析-下载资源排行-下载资源排行榜，资源统计与分析-上传资源排行-上传资源排行榜】
    @RequestMapping(value = "/linkTrafficRank", method = arrayOf(RequestMethod.POST))
    fun linkTrafficRank(@RequestBody rangeCondition: RangeCondition, topCount: Int = 10, type: String = "total"): CommonResponse {
        var dataMap = mutableMapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(emptyMap(), emptyMap(), listOf(rangeCondition))
            val sumFieldName = when (type) {
                "down" -> "long_downlink_traffic"
                "up" -> "long_uplink_traffic"
                else -> "long_total_traffic"
            }
            val aggs = AggregationBuilders.terms("distinct")
                    .field("resource_name.keyword").size(topCount).order(BucketOrder.aggregation("distinct", false))
                    .subAggregation(AggregationBuilders.sum("totalTraffic").field(sumFieldName))
                    .order(BucketOrder.aggregation("totalTraffic", false))

            val request = SearchRequest().indices(indicesName)
                    .source(SearchSourceBuilder()
                            .fetchSource(false)
                            .size(0)
                            .query(qb)
                            .aggregation(aggs)
                            .timeout(TimeValue(2, TimeUnit.MINUTES)))

            val buckets = client.search(request).aggregations.get<Terms>("distinct").buckets
            dataMap = mutableMapOf(
                    "xAxis" to Array<String>(buckets.size, { x -> buckets[x].keyAsString }),
                    "yAxis" to Array<String>(buckets.size, { x -> buckets[x].aggregations.get<Sum>("totalTraffic").valueAsString })
            )
        }
        return CommonResponse(dataMap, elapsed_time_)
    }

    // 资源统计分析-下载资源排行-下载文件格式占比，资源统计分析-上传资源排行-上传文件格式占比
    @RequestMapping(value = "/fileFormatPie", method = arrayOf(RequestMethod.POST))
    fun fileFormatPie(@RequestBody rangeCondition: RangeCondition, topCount: Int = 10, type: String? = "total"): CommonResponse {
        var dataMap = mutableMapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(emptyMap(), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("distinct")
                    .script(Script("doc['keyword_file_format'].value")).size(topCount)
                    .subAggregation(AggregationBuilders.count("fileFormatCount")
                            .field("keyword_file_format"))
                    .order(BucketOrder.aggregation("fileFormatCount", false))
            val request = SearchRequest().indices(indicesName)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb)
                            .aggregation(aggs).timeout(TimeValue(2, TimeUnit.MINUTES)))
            LOGGER.info(request.toString())
            val buckets = client.search(request).aggregations.get<Terms>("distinct").buckets
            val m = buckets.map { it.keyAsString to it.docCount.toInt() }.toMap()

            val _total = buckets.map { it.docCount.toInt() }.sum()
            val fileTotal = totalResStatistics(rangeCondition)
            dataMap.putAll(m)
            dataMap.put("others", fileTotal - _total)

        }
        return CommonResponse(dataMap, elapsed_time_)
    }

    // 流量统计与分析-用户、应用、用户组
    @RequestMapping(value = "/trafficRank", method = arrayOf(RequestMethod.POST))
    fun trafficRank(@RequestBody rangeCondition: RangeCondition, topCount: Int, type: String): CommonResponse {
        return tranfficStatistics(rangeCondition, topCount, type)
    }

    // 流量统计与分析-用户流量统计/应用资源流量统计
    @RequestMapping(value = "/trafficGroupStatics", method = arrayOf(RequestMethod.POST))
    fun trafficGroupStatics(@RequestBody condition: Condition, type: String): CommonResponse {
        val pager: Pager = Pager(condition.currentPage, condition.pageSize)
        val elapsed_time_ = measureTimeMillis {

            val qb = queryBuilders(condition.preciseConditions, condition.ambiguousConditions, condition.rangeConditionList)
            var totalAggr = AggregationBuilders.terms("distinct")
                    .script(Script("doc['user_name.keyword'].value + '  ' + doc['user_group.keyword'].value"))
            if ("resource".equals(type)) {
                totalAggr = AggregationBuilders.terms("distinct")
                        .script(Script("doc['resource_name.keyword'].value + '  ' + doc['uri.keyword'].value"))
            }
            val _totalRequest = SearchRequest("res").source(
                    SearchSourceBuilder().fetchSource(false).query(qb).aggregation(totalAggr.size(MAX_AGGR_COUNT))
            )
            LOGGER.info("_totalRequest === ${_totalRequest.source().toString()}")
            val total = client.search(_totalRequest).aggregations.get<Terms>("distinct").buckets.size
            pager.totalHits = if (total > MAX_AGGR_COUNT) MAX_AGGR_COUNT.toLong() else total.toLong()

            var startIndex = (condition.currentPage - 1) * condition.pageSize
            var endIndex = condition.currentPage * condition.pageSize
            if (pager.totalHits < startIndex) {
                pager.currentPage = 1
                startIndex = 0
                endIndex = pager.pageSize
            }
            totalAggr.subAggregation(AggregationBuilders.min("firstVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                    .subAggregation(AggregationBuilders.max("lastVisitDate").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
                    .subAggregation(AggregationBuilders.sum("totalDownloadTraffic").field("long_downlink_traffic"))
                    .subAggregation(AggregationBuilders.sum("totalUploadTraffic").field("long_uplink_traffic"))
                    .subAggregation(AggregationBuilders.sum("totalTraffic").field("long_total_traffic"))
            totalAggr.size(endIndex).order(BucketOrder.aggregation("totalTraffic", false))
            val _dataRequest = SearchRequest().indices("res").source(
                    SearchSourceBuilder.searchSource()
                            .fetchSource(false).size(0).query(qb)
                            .aggregation(totalAggr)
                            .timeout(TimeValue(2, TimeUnit.MINUTES))
            )
            LOGGER.info("_dataRequest === ${_dataRequest.source().toString()}")
            val searchResponse = client.search(_dataRequest)

            val terms: Terms = searchResponse.getAggregations().get("distinct")
            val buckets = terms.buckets

            pager.pageDatas = List(buckets.size - startIndex, { y ->
                var x = y + startIndex

                val name2Origin = buckets.get(x).keyAsString.split("  ")[1]
                var _name2 = buckets.get(x).keyAsString.split("  ")[1]
                if (_name2.isNotBlank() && _name2.contains("\\x")) {
                    _name2 = CommonStringUtils.hexStringToString(_name2.replace("\\x", ""))
                }

                mapOf(
                        "name" to buckets.get(x).keyAsString.split("  ")[0],
                        "name2" to _name2,
                        "name2Origin" to name2Origin,
                        "lastVisitDate" to buckets.get(x).aggregations.get<Max>("lastVisitDate").valueAsString,
                        "firstVisitDate" to buckets.get(x).aggregations.get<Min>("firstVisitDate").valueAsString,
                        "visitCount" to buckets.get(x).docCount,
                        "totalDownloadTraffic" to buckets.get(x).aggregations.get<Sum>("totalDownloadTraffic").valueAsString,
                        "totalUploadTraffic" to buckets.get(x).aggregations.get<Sum>("totalUploadTraffic").valueAsString,
                        "totalTraffic" to buckets.get(x).aggregations.get<Sum>("totalTraffic").valueAsString
                )
            })
        }
        return CommonResponse(pager, elapsed_time_)
    }

    // 资源总数统计
    private fun totalResStatistics(rangeCondition: RangeCondition): Long {
        val qb = queryBuilders(emptyMap(), emptyMap(), listOf(rangeCondition))
        val aggs = AggregationBuilders.count("fileFormat").field("keyword_file_format")
        val request = SearchRequest().indices(indicesName)
                .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb).aggregation(aggs))
        return client.search(request).hits.totalHits
    }

    private fun tranfficStatistics(rangeCondition: RangeCondition, topCount: Int, type: String): CommonResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(emptyMap(), emptyMap(), listOf(rangeCondition))
            val aggs = when (type) {
                "userName" -> userNameTrafficAggrs(topCount)
                "userGroup" -> userGroupTrafficAggrs(topCount)
                else -> resTrafficAggrs(topCount)
            }
            val request = SearchRequest().indices(indicesName)
                    .source(SearchSourceBuilder().fetchSource(false).size(0).query(qb)
                            .aggregation(aggs).timeout(TimeValue(2, TimeUnit.MINUTES)))

            LOGGER.info(request.toString())
            val buckets = client.search(request).aggregations.get<Terms>("groupby").buckets
            dataMap = mapOf(
                    "xAxis" to Array<String>(buckets.size, { x -> buckets[x].keyAsString }),
                    "yAxis" to Array<String>(buckets.size, { x -> buckets[x].aggregations.get<Sum>("trafficTotal").valueAsString })
            )
        }
        return CommonResponse(dataMap, elapsed_time_)
    }

    private fun userNameTrafficAggrs(topCount: Int): AggregationBuilder {
        return AggregationBuilders.terms("groupby").field("user_name.keyword").size(topCount)
                .subAggregation(AggregationBuilders.sum("trafficTotal").field("long_total_traffic"))
                .order(BucketOrder.aggregation("trafficTotal", false))
    }

    private fun userGroupTrafficAggrs(topCount: Int): AggregationBuilder {
        return AggregationBuilders.terms("groupby").field("user_group.keyword").size(topCount)
                .subAggregation(AggregationBuilders.sum("trafficTotal").field("long_total_traffic"))
                .order(BucketOrder.aggregation("trafficTotal", false))
    }

    private fun resTrafficAggrs(topCount: Int): AggregationBuilder {
        return AggregationBuilders.terms("groupby").field("resource_name.keyword").size(topCount)
                .subAggregation(AggregationBuilders.sum("trafficTotal").field("long_total_traffic"))
                .order(BucketOrder.aggregation("trafficTotal", false))
    }
}

@RestController
@RequestMapping("/userlog")
class UserLogController : BaseController() {
    // 【统计与分析-用户登录与统计分析-用户登录排行】
    @RequestMapping(value = "/userLoginRank", method = arrayOf(RequestMethod.POST))
    fun getUserLoginRank(@RequestBody rangeCondition: RangeCondition, topCount: Int): CommonResponse {
        var dataMap = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(getConditionMap(emptyMap()), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("distinct")
                    .script(Script("doc['user_name.keyword'].values[0]")).size(topCount)
                    .subAggregation(AggregationBuilders.count("userNameCount").field("user_name.keyword"))
                    .order(BucketOrder.aggregation("userNameCount", false))
            val searchReq = SearchRequest().indices("user")
                    .source(SearchSourceBuilder().fetchSource(false).query(qb).aggregation(aggs).timeout(TimeValue(2, TimeUnit.MINUTES)))
            val buckets = client.search(searchReq).aggregations.get<Terms>("distinct").buckets
            // 将查询结果倒叙排列，前段需要的数据结构
            val xAxis = Array(buckets.size, { x -> buckets[x].keyAsString })
            val yAxis = Array(buckets.size, { x -> buckets[x].docCount.toInt() })
            dataMap = mapOf("xAxis" to xAxis, "yAxis" to yAxis)
        }
        return CommonResponse(dataMap, elapsed_time_)
    }

    // 【统计与分析-用户登录与统计分析-终端系统占比】
    @RequestMapping(value = "/terminalPie", method = arrayOf(RequestMethod.POST))
    fun terminalPie(@RequestBody rangeCondition: RangeCondition): CommonResponse {
        var map = mapOf<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(getConditionMap(emptyMap()), emptyMap(), listOf(rangeCondition))
            val aggs = AggregationBuilders.terms("deviceOs")
                    .field("device_os.keyword").size(Int.MAX_VALUE)
                    .subAggregation(AggregationBuilders.count("deviceOsCount").field("device_os.keyword"))
                    .order(BucketOrder.aggregation("deviceOsCount", false))
            val searchReq = SearchRequest("user")
                    .source(SearchSourceBuilder().fetchSource(false)
                            .query(qb).aggregation(aggs).timeout(TimeValue(2, TimeUnit.MINUTES)))
            val resp = client.search(searchReq)
            val buckets = resp.aggregations.get<Terms>("deviceOs").buckets
            map = buckets.map { it.keyAsString to it.docCount.toInt() }.toMap()
        }
        return CommonResponse(map, elapsed_time_)
    }

    @RequestMapping(value = "userLoginGroupStatics", method = arrayOf(RequestMethod.POST))
    fun getUserLoginGroupStatics(@RequestBody condition: Condition): CommonResponse {
        var pager: Pager = Pager(condition.currentPage, condition.pageSize)
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(getConditionMap(condition.preciseConditions), condition.ambiguousConditions, condition.rangeConditionList)
            val totalAggr = AggregationBuilders.terms("distinct")
                    .script(Script("doc['user_name.keyword'].values[0] + '  ' + doc['user_group.keyword'].values[0]"))
            val _totalRequest = SearchRequest("user").source(
                    SearchSourceBuilder().fetchSource(false).query(qb).aggregation(totalAggr.size(Int.MAX_VALUE))
            )
            LOGGER.info("_totalRequest === ${_totalRequest.source().toString()}")
            val total = client.search(_totalRequest).aggregations.get<Terms>("distinct").buckets.size
            pager.totalHits = if (total > MAX_AGGR_COUNT) MAX_AGGR_COUNT.toLong() else total.toLong()

            var startIndex = (condition.currentPage - 1) * condition.pageSize
            var endIndex = condition.currentPage * condition.pageSize
            if (pager.totalHits < startIndex) {
                pager.currentPage = 1
                startIndex = 0
                endIndex = pager.pageSize
            }
            totalAggr.subAggregation(AggregationBuilders.max("_date_max_by").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
            totalAggr.subAggregation(AggregationBuilders.min("_date_min_by").field("@timestamp").format("yyyy-MM-dd HH:mm:ss"))
            totalAggr.subAggregation(AggregationBuilders.count("_count").field("@timestamp"))
            totalAggr.size(endIndex).order(BucketOrder.aggregation("_count", false))
            val _dataRequest = SearchRequest().indices("user").source(
                    SearchSourceBuilder.searchSource().fetchSource(false).size(0).query(qb).aggregation(totalAggr).timeout(TimeValue(2, TimeUnit.MINUTES))
            )
            LOGGER.info("_dataRequest === ${_dataRequest.source().toString()}")
            val searchResponse = client.search(_dataRequest)

            val terms: Terms = searchResponse.getAggregations().get("distinct")
            val buckets = terms.buckets

            pager.pageDatas = List(buckets.size - startIndex, { y ->
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
        return CommonResponse(pager, elapsed_time_)
    }

    @RequestMapping(value = "userLoginDetails", method = arrayOf(RequestMethod.POST))
    fun userLoginDetails(@RequestBody condition: Condition): CommonResponse {
        val pager: Pager = Pager(condition.currentPage, condition.pageSize)
        val elapsed_time_ = measureTimeMillis {
            val qb = queryBuilders(getConditionMap(condition.preciseConditions),
                    condition.ambiguousConditions, condition.rangeConditionList)

            val _totalRequest = SearchRequest("user").source(
                    SearchSourceBuilder().fetchSource(false).query(qb))
            LOGGER.info("_totalRequest === ${_totalRequest.source().toString()}")
            val total = client.search(_totalRequest).hits.totalHits
            pager.totalHits = if (total > MAX_AGGR_COUNT) MAX_AGGR_COUNT.toLong() else total.toLong()

            val startIndex = (condition.currentPage - 1) * condition.pageSize
            val endIndex = condition.currentPage * condition.pageSize
            val searchRequest = SearchRequest().indices("user")
                    .source(SearchSourceBuilder.searchSource()
                            .from(startIndex).size(endIndex).query(qb).sort("log_timestamp.keyword", SortOrder.DESC))
            val searchResponse = client.search(searchRequest)
            val searchHits = searchResponse.getHits().getHits()
            val list = List<Map<String, Any?>>(searchHits.size, { x ->
                reConvertToMap(searchHits.get(x).sourceAsMap, "userLog")
            })
            pager.pageDatas = list
        }
        return CommonResponse(pager, elapsed_time_)
    }

    private fun getConditionMap(m: Map<String, Array<String>>): Map<String, Array<String>> {
        val params = mutableMapOf<String, Array<String>>()
        params.put("status", arrayOf("SUCCESS"))
        params.put("operation.keyword", arrayOf("LOGIN"))
        if (null != m) {
            params.putAll(m)
        }
        return params
    }
}