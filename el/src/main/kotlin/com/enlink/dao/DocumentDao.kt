package com.enlink.dao

import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/25 11:04
 * @description
 */
@Component
open class DocumentDao {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    lateinit var client: RestHighLevelClient

    fun index(index: String, jsonString: String, type: String = "doc", id: String = "1") {
        val request = IndexRequest(index, type, id)
                .source(jsonString, XContentType.JSON)
                .opType(DocWriteRequest.OpType.CREATE)
        try {
            val response: IndexResponse = client.index(request)
        } catch (e: ElasticsearchException) {
            if (e.status() == RestStatus.CONFLICT) {
                LOGGER.info("索引已存在，索引内容：$index == $type == $id == $jsonString")
            }
        }
    }

    fun get(index: String, type: String = "doc", id: String = "1"): String {
        val request = GetRequest(index, type, id)
        val response: GetResponse = client.get(request)
        return response.sourceAsString
    }

    fun exists(index: String, type: String = "doc", id: String = "1"): Boolean {
        val request = GetRequest(index, type, id)
        return client.exists(request)
    }

    fun delete(index: String, type: String = "doc", id: String = "1"): String {
        val request = DeleteRequest(index, type, id)
        request.timeout("2m")
        val response = client.delete(request)
        return response.result.name
    }

    fun update(index: String, jsonString: String, type: String = "doc", id: String = "1"): Int {
        val request = UpdateRequest(index, type, id)
        request.doc(jsonString, XContentType.JSON)
        val response: UpdateResponse = client.update(request)
        return response.status().status
    }

    fun bulk(jsonBytesArray: ByteArray): String {
        val request = BulkRequest()
        request.add(jsonBytesArray, 0, jsonBytesArray.size, XContentType.JSON)
        val response = client.bulk(request)
        return response.buildFailureMessage()
    }
}