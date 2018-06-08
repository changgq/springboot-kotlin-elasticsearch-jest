package com.es.services.impl

import com.es.common.GsonUtils
import com.es.model.LogBackupsRecord
import com.es.model.LogDeleteRecord
import com.es.model.LogRecoverRecord
import com.es.model.LogTimedTask
import com.es.services.LogBackupsRecordService
import com.es.services.LogDeleteRecordService
import com.es.services.LogRecoverRecordService
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RestHighLevelClient

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/7 21:03
 * @description
 */
class LogDeleteRecordServiceImpl(val client: RestHighLevelClient) : LogDeleteRecordService {
    private val INDEX_NAME: String = "log_delete_record"
    private val TYPE_NAME: String = "LOG_DELETE_RECORD"

    override fun insert(record: LogDeleteRecord): Boolean {
        val indexReq = IndexRequest(INDEX_NAME, TYPE_NAME, record.id).source(record.toMap())
        val resp = client.index(indexReq)
        return resp.status().status == 200
    }

    override fun update(record: LogDeleteRecord): Boolean {
        val updateResp = client.update(UpdateRequest(INDEX_NAME, TYPE_NAME, record.id).doc(record.toMap()))
        return updateResp.status().status == 200
    }

    override fun deleteById(id: String): Boolean {
        val deleteReq = DeleteRequest(INDEX_NAME, TYPE_NAME, id)
        val resp = client.delete(deleteReq)
        return resp.status().status == 200
    }

    override fun findById(id: String): LogDeleteRecord {
        val req = GetRequest(INDEX_NAME, TYPE_NAME, id)
        val resp = client.get(req)
        return GsonUtils.reConvert(resp.sourceAsString, LogDeleteRecord::class.java)
    }

}