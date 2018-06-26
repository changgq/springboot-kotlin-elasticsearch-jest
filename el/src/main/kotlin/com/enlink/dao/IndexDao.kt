package com.enlink.dao

import com.enlink.config.properties.ElasticProps
import com.enlink.platform.GsonUtils
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.support.IndicesOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.metrics.max.Max
import org.elasticsearch.search.aggregations.metrics.min.Min
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.nio.charset.Charset

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/25 11:02
 * @description
 */
@Component
open class IndexDao {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    val charset: Charset = Charset.defaultCharset()
    @Autowired
    lateinit var pros: ElasticProps
    @Autowired
    lateinit var client: RestHighLevelClient
    @Autowired
    lateinit var okHttpClient: OkHttpClient

    /**
     * 功能描述: 重载索引，将旧索引重新导入到新的索引中
     *
     * @param jsons
     * @return
     */
    fun reindex(jsons: String): String {
        val res: okhttp3.Response = okHttpClient.newCall(Request.Builder()
                .header("Content-Type", "application/json")
                .url("${pros.uris[0]}/_reindex")
                .post(okhttp3.RequestBody.create(MediaType.parse("application/json"), jsons))
                .build()).execute()
        res.header("Content-Type", "application/json")
        var result: String = ""
        if (res.isSuccessful) {
            val responseBody = res.body()
            if (responseBody != null) {
                result = responseBody.source().readString(charset)
                responseBody.close()
            }
        }
        return result
    }

    fun existsIndex(indices: String): Boolean {
        val request = GetIndexRequest().indices(indices)
        return client.indices().exists(request)
    }

    fun deleteIndex(indices: String): Boolean {
        try {
            val request = DeleteIndexRequest(indices)
                    .timeout("2m")
                    .indicesOptions(IndicesOptions.lenientExpandOpen())
            val resp = client.indices().delete(request)
            return resp.isAcknowledged
        } catch (exception: ElasticsearchException) {
            if (exception.status() === RestStatus.NOT_FOUND) {
                LOGGER.info("删除的索引 $indices 不存在！")
            }
        }
        return true
    }

    fun openIndex(indices: String): Boolean {
        val request = OpenIndexRequest(indices)
                .timeout("2m")
                .indicesOptions(IndicesOptions.lenientExpandOpen())
        val resp = client.indices().open(request)
        return resp.isAcknowledged
    }

    fun closeIndex(indices: String): Boolean {
        val request = CloseIndexRequest(indices)
                .timeout("2m")
                .indicesOptions(IndicesOptions.lenientExpandOpen())
        val resp = client.indices().close(request)
        return resp.isAcknowledged
    }

    fun createIndex(indices: String, mappings: String) {
        try {
            LOGGER.info("索引 $indices 不存在, 索引创建开始......")
            val req = CreateIndexRequest(indices)
            req.source(mappings, XContentType.JSON)
            val resp = client.indices().create(req)
            LOGGER.info("索引创建成功,所有节点确认状态： [${resp.isAcknowledged}]")
            LOGGER.info("索引创建成功,超时之前所有分片拷贝完成状态： [${resp.isShardsAcked}]")
            LOGGER.info("索引 $indices 创建完成！")
        } catch (e: Exception) {
            LOGGER.error("索引 $indices 创建失败！，失败原因：${e.message}")
        }
    }

    fun initIndex(indices: String) {
        if (!existsIndex(indices)) {
            val f = File(this::class.java.classLoader.getResource("elastic/mappings/$indices.json").path)
            if (!f.exists()) throw Exception("Indices $indices create fial, the mappings file is not exists")
            createIndex(indices, f.readText())
        }
    }

    fun indexMinDate(indices: String): Long {
        val request = SearchRequest(indices)
                .source(SearchSourceBuilder().fetchSource(false)
                        .aggregation(AggregationBuilders.min("min")
                                .field("@timestamp").format("yyyy-MM-dd")))
        LOGGER.info(request.toString())
        val response = client.search(request)
        return response.aggregations.get<Min>("min").value.toLong()
    }

    fun indexMaxDate(indices: String): Long {
        val request = SearchRequest(indices)
                .source(SearchSourceBuilder().fetchSource(false)
                        .aggregation(AggregationBuilders.max("max")
                                .field("@timestamp").format("yyyy-MM-dd")))
        LOGGER.info(request.toString())
        val response = client.search(request)
        return response.aggregations.get<Max>("max").value.toLong()
    }

    fun putTemplate(templateName: String, jsons: String): Boolean {
        val res: okhttp3.Response = okHttpClient.newCall(Request.Builder()
                .header("Content-Type", "application/json")
                .url("${pros.uris[0]}/_template/$templateName")
                .put(okhttp3.RequestBody.create(MediaType.parse("application/json"), jsons))
                .build()).execute()
        res.header("Content-Type", "application/json")
        var result: String = ""
        if (res.isSuccessful) {
            val responseBody = res.body()
            if (responseBody != null) {
                result = responseBody.source().readString(charset)
                responseBody.close()
            }
        }
        val ro = GsonUtils.reConvert(result, Map::class.java)
        return ro.get("acknowledged") as Boolean
    }
}