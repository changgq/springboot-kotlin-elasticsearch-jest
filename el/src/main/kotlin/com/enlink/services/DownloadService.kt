package com.enlink.services

import com.enlink.config.properties.PathProps
import com.enlink.platform.*
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


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

    fun downloadNew(conditions: List<DownloadCondition>): Array<String> {
        val pathArrays = mutableListOf<String>()
        conditions.forEach { x ->
            val index = IndexMappings.Index_mappings.get(x.logType)
            val qb = QueryBuilders.boolQuery()
            qb.must(QueryBuilders.matchQuery("", ""))
            // 范围查询
            x.rangeConditionList?.forEach { it ->
                when (it.type) {
                    RangeCondition.Type.DATE -> {
                        qb.must(QueryBuilders.rangeQuery("backups_date").gte(it.gteValue).lte(it.lteValue)
                                .timeZone(it.timeZone))
                    }
                    else -> {
                        qb.must(QueryBuilders.rangeQuery(IndexMappings.Index_field_mappings[it.conditionName]).gte(it.gteValue).lte(it.lteValue))
                    }
                }
            }
            // 查询结果不能超过一年
            val request = SearchRequest()
                    .indices(index)
                    .source(SearchSourceBuilder().query(qb).size(366))
            LOGGER.info("查询条件：${request.source().toString()}")
            val response = client.search(request)
            if (response.hits.hits.size > 0) {
                response.hits.hits.forEach { y ->
                    pathArrays.add(y.sourceAsMap.get("file_path").toString())
                }
            }
        }
        return pathArrays.toTypedArray()
    }

    /**
     * 功能描述: 获取Excel的头字段
     */
    fun getHeaders(index: String): Map<String, String> = when (index) {
        "user" -> {
            mapOf<String, String>(
                    "log_timestamp" to "时间",
                    "user_name" to "用户名",
                    "user_group" to "用户组",
                    "keyword_user_auth" to "用户权限",
                    "operation" to "操作",
                    "keyword_status" to "登录信息",
                    "ip_address" to "IP地址",
                    "client_info" to "硬件地址",
                    "certificate_server" to "认证服务器",
                    "link_interface" to "链接隧道"
            )
        }
        "admin" -> {
            mapOf(
                    "log_timestamp" to "时间",
                    "user_name" to "用户名",
                    "user_group" to "用户组",
                    "keyword_user_auth" to "用户权限",
                    "operation" to "操作",
                    "log_info" to "登录信息",
                    "ip_address" to "ip地址",
                    "mac_address" to "硬件地址"
            )
        }
        "res" -> {
            mapOf(
                    "log_timestamp" to "时间",
                    "user_name" to "用户名",
                    "user_group" to "用户组",
                    "resource_name" to "资源名称",
                    "uri" to "uri地址",
                    "long_uplink_traffic" to "上行流量",
                    "long_downlink_traffic" to "下行流量",
                    "long_total_traffic" to "总流量",
                    "keyword_file_format" to "文件格式",
                    "browser_info" to "浏览器信息",
                    "url_http" to "url/Http协议"
            )
        }
        "system" -> {
            mapOf(
                    "log_timestamp" to "时间",
                    "operate_type" to "操作",
                    "cpu" to "CPU",
                    "memory" to "内存",
                    "disk" to "硬盘",
                    "fan" to "风扇",
                    "temperature" to "温度"
            )
        }
        else -> {
            mapOf(
                    "log_timestamp" to "时间",
                    "user_name" to "用户名",
                    "user_group" to "用户组",
                    "keyword_user_auth" to "用户权限",
                    "operation" to "操作",
                    "keyword_status" to "登录信息",
                    "ip_address" to "IP地址",
                    "client_info" to "硬件地址",
                    "certificate_server" to "认证服务器",
                    "link_interface" to "链接隧道"
            )
        }
    }
}