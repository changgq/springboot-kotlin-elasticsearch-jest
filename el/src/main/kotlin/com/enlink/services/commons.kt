package com.enlink.services

import com.enlink.config.ElasticsearchProperties
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.support.IndicesOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import com.sun.corba.se.spi.presentation.rmi.StubAdapter.request
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest
import org.elasticsearch.common.lucene.uid.Versions.NOT_FOUND
import org.elasticsearch.rest.RestStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.sun.corba.se.spi.presentation.rmi.StubAdapter.request
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest
import org.elasticsearch.common.xcontent.XContentType
import java.io.File


/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/22 22:17
 * @description
 */
@Service
open class IndexService {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var client: RestHighLevelClient
    @Autowired
    lateinit var okHttpClient: OkHttpClient
    @Autowired
    lateinit var pros: ElasticsearchProperties

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
                result = responseBody.source().readString(Charset.forName("UTF-8"))
                responseBody.close()
            }
        }
        return result
    }

    fun existsIndex(indices: String): Boolean {
        val request = GetIndexRequest()
                .indices(indices)
                .local(false)
                .humanReadable(true)
                .includeDefaults(false)
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
        val request = OpenIndexRequest("index")
                .timeout("2m")
                .indicesOptions(IndicesOptions.lenientExpandOpen())
        val resp = client.indices().open(request)
        return resp.isAcknowledged
    }

    fun closeIndex(indices: String): Boolean {
        val request = CloseIndexRequest("index")
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
            createIndex(indices, f.readText(Charset.defaultCharset()))
        }
    }
}