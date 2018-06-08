package com.es.services.impl

import com.es.common.GsonUtils
import com.es.model.LogSetting
import com.es.model.LogTimedTask
import com.es.services.LogTimedTaskService
import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.action.search.SearchRequest


/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/7 20:47
 * @description
 */
class LogTimedTaskServiceImpl(val client: RestHighLevelClient) : LogTimedTaskService {
    private val INDEX_NAME = "log_timed_task"
    private val TYPE_NAME = "LOG_TIMED_TASK"

    override fun insert(task: LogTimedTask): Boolean {
        val indexReq = IndexRequest(INDEX_NAME, TYPE_NAME, task.id).source(task.toMap())
        val resp = client.index(indexReq)
        return resp.status().status == 200
    }

    override fun update(task: LogTimedTask): Boolean {
        val updateResp = client.update(UpdateRequest(INDEX_NAME, TYPE_NAME, task.id).doc(task.toMap()))
        return updateResp.status().status == 200
    }

    override fun deleteById(id: String): Boolean {
        val deleteReq = DeleteRequest(INDEX_NAME, TYPE_NAME, id)
        val resp = client.delete(deleteReq)
        return resp.status().status == 200
    }

    override fun findById(id: String): LogTimedTask {
        val req = GetRequest(INDEX_NAME, TYPE_NAME, id)
        val resp = client.get(req)
        return GsonUtils.reConvert(resp.sourceAsString, LogTimedTask::class.java)
    }

    override fun findAll(): List<LogTimedTask> {
        val searchRequest = SearchRequest(INDEX_NAME).types(TYPE_NAME)
        val resp = client.search(searchRequest)
        val hits = resp.hits.hits
        return List<LogTimedTask>(hits.size, { x -> GsonUtils.reConvert(hits[x].sourceAsString, LogTimedTask::class.java) })
    }
}