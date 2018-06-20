package com.es.api

import com.es.common.ApiResponse
import com.es.services.SystemLogService
import com.es.services.impl.SystemLogServiceImpl
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/1 14:27
 * @description
 */
@RestController
@RequestMapping("/sysLog")
class SystemLogAction(highLevelClient: RestHighLevelClient) : BaseAction(highLevelClient) {
    val systemLogService: SystemLogService = SystemLogServiceImpl(highLevelClient)

    // 获取网卡总流量
    @RequestMapping(value = "/getNowFlow", method = arrayOf(RequestMethod.POST))
    fun getNowFlow(): ApiResponse {
        return systemLogService.getNowNetFlow()
    }

    @RequestMapping(value = "/getFlowList", method = arrayOf(RequestMethod.POST))
    fun getFlowList(): ApiResponse {
        return systemLogService.getNetFlowList()
    }
}