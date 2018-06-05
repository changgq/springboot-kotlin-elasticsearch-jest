package com.es.api

import com.es.common.ApiResponse
import com.es.common.RangeCondition
import com.es.common.SearchCondition
import com.es.services.UserLogService
import com.es.services.impl.UserLogServiceImpl
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * 功能描述：用户日志
 *
 * @auther changgq
 * @date 2018/5/31 13:27
 * @description
 */
@RestController
@RequestMapping("/userLog")
class UserLogAction(highLevelClient: RestHighLevelClient) : BaseAction(highLevelClient) {
    val userLogService: UserLogService = UserLogServiceImpl(highLevelClient)

    @RequestMapping(value = "/userLoginRank", method = arrayOf(RequestMethod.POST))
    fun getUserLoginRank(@RequestBody rangeCondition: RangeCondition, topCount: Int): ApiResponse {
        return userLogService.getUserLoginRank(rangeCondition, topCount)
    }

    //用户登录前N名，及其流量总数
    @RequestMapping(value = "/userLoginRankAndFlow", method = arrayOf(RequestMethod.POST))
    fun getUserLoginRankAndFlow(@RequestBody rangeCondition: RangeCondition, topCount: Int): ApiResponse {
        return userLogService.getUserLoginRankAndFlow(rangeCondition, topCount)
    }

    @RequestMapping(value = "/terminalPie", method = arrayOf(RequestMethod.POST))
    fun getTerminalPie(@RequestBody rangeCondition: RangeCondition): ApiResponse {
        return userLogService.getTerminalPie(rangeCondition)
    }

    //根据用户名获取登录总次数
    @RequestMapping(value = "userLoginCount", method = arrayOf(RequestMethod.POST))
    fun getUserLoginCount(@RequestBody condition: SearchCondition, userName: String): ApiResponse {
        return userLogService.getUserLoginCount(condition, userName)
    }

    @RequestMapping(value = "userLoginGroupStatics", method = arrayOf(RequestMethod.POST))
    fun getUserLoginGroupStatics(@RequestBody condition: SearchCondition): ApiResponse {
        return userLogService.getUserLoginGroupStatics(condition)
    }

    @RequestMapping(value = "userLoginDetails", method = arrayOf(RequestMethod.POST))
    fun getUserLoginDetails(@RequestBody condition: SearchCondition): ApiResponse {
        return userLogService.getUserLoginDetailsCount(condition)
    }
}