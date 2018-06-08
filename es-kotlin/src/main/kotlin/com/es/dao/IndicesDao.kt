package com.es.dao

import com.es.model.LogSetting
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/6 16:52
 * @description
 */
object IndicesDao {
    val LOGGER: Logger = LoggerFactory.getLogger(IndicesDao::class.java)

    fun createIndex(indexName: String, mappings: String, highLevelClient: RestHighLevelClient) {
        try {
            LOGGER.info("索引 $indexName 不存在, 索引创建开始......")
            val req = CreateIndexRequest(indexName)
            req.source(mappings, XContentType.JSON)
            val resp = highLevelClient.indices().create(req)
            LOGGER.info("索引创建成功,所有节点确认状态： [${resp.isAcknowledged}]")
            LOGGER.info("索引创建成功,超时之前所有分片拷贝完成状态： [${resp.isShardsAcked}]")
            LOGGER.info("索引 $indexName 不存在, 索引创建完成！")
        } catch (e: Exception) {
            LOGGER.error("索引 $indexName 不存在, 索引创建失败！，失败原因：${e.message}")
        }
    }


    fun insertIndex(indexName: String, typeName: String, id: String, source: Map<String, Any>, highLevelClient: RestHighLevelClient) {
        val indexRequest = IndexRequest(indexName, typeName, id)
        indexRequest.source(LogSetting().toMap())
        indexRequest.opType(DocWriteRequest.OpType.CREATE)
        highLevelClient.index(indexRequest)
    }

    fun updateIndex(indexName: String, typeName: String, id: String, source: Map<String, Any>, highLevelClient: RestHighLevelClient) {
        val indexRequest = IndexRequest(indexName, indexName.toUpperCase(), "1")
        indexRequest.source(LogSetting().toMap())
        indexRequest.opType(DocWriteRequest.OpType.UPDATE)
        highLevelClient.index(indexRequest)
    }

    fun deleteIndex(indexName: String, highLevelClient: RestHighLevelClient) {
        highLevelClient.delete(DeleteRequest(indexName))
    }
}