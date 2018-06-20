package com.enlink.controller

import com.enlink.model.LogSetting
import com.enlink.platform.CommonResponse
import com.enlink.platform.Condition
import com.enlink.platform.GsonUtils
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHeader
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.charset.Charset
import kotlin.math.log

/**
 * 功能描述：日志基础接口
 */
@RestController
@RequestMapping("/log")
class LogController : BaseController() {

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
}

/**
 * 功能描述：日志设置接口
 */
@RestController
@RequestMapping("/logconfig")
class LogConfigAction : BaseController() {
    private val logConfigPath = "d://usr/local/enlink/logJson.config"

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
        val rel = client.update(UpdateRequest("log_setting", "LOG_SETTING", logSetting.id).doc(logSetting.toMap()))
        // 保存日志文件到服务器
        val f = File(logConfigPath)
        if (!f.exists()) {
            f.parentFile.mkdirs()
        }
        f.writeText(GsonUtils.convert(logSetting)!!, Charset.defaultCharset())
        return CommonResponse(rel.status().status == 200)
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
        val resp = client.get(GetRequest("log_setting", "LOG_SETTING", "1"))
        return CommonResponse(GsonUtils.reConvert(resp.sourceAsString, LogSetting::class.java))
    }
}