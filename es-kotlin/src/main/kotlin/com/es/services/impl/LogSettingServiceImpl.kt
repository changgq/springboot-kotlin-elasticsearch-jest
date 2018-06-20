package com.es.services.impl

import com.es.common.GsonUtils
import com.es.model.LogSetting
import com.es.services.LogSettingService
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.stereotype.Service

/**
 * 功能描述：日志设置
 *
 * @auther changgq
 * @date 2018/6/7 19:13
 * @description
 */
@Service
class LogSettingServiceImpl(val client: RestHighLevelClient) : LogSettingService {
    private val INDEX_NAME = "log_setting"
    private val TYPE_NAME = "LOG_SETTING"

    override fun update(ls: LogSetting): Boolean {
        val updateResp = client.update(UpdateRequest(INDEX_NAME, TYPE_NAME, ls.id).doc(ls.toMap()))
        return updateResp.status().status == 200
    }

    override fun getById(id: String): LogSetting {
        val req = GetRequest(INDEX_NAME, TYPE_NAME, id)
        val resp = client.get(req)
        return GsonUtils.reConvert(resp.sourceAsString, LogSetting::class.java)
    }

    override fun findAll(): List<LogSetting> {
        val searchRequest = SearchRequest(INDEX_NAME).types(TYPE_NAME)
        val resp = client.search(searchRequest)
        val hits = resp.hits.hits
        return List<LogSetting>(hits.size, { x -> GsonUtils.reConvert(hits[x].sourceAsString, LogSetting::class.java) })
    }
}