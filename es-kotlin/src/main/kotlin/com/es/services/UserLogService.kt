package com.es.services

import com.es.common.ApiResponse
import com.es.common.Condition
import com.es.common.RangeCondition
import com.es.common.SearchCondition

/**
 * 功能描述：用户日志业务层接口
 *
 * @auther changgq
 * @date 2018/5/31 13:31
 * @description
 */
interface UserLogService {
    /**
     * 功能描述: 获取用户登录排行柱状图数据
     * @auther changgq
     * @date 2018/5/31 13:35
     *
     * @param rangeCondition
     * @param topCount
     * @return
     */
    fun getUserLoginRank(rangeCondition: RangeCondition, topCount: Int): ApiResponse

    /**
     * 功能描述: 用户登录前N名，及其流量总数
     * @auther changgq
     * @date 2018/5/31 16:12
     *
     * @param rangeCondition
     * @param topCount
     * @return
     */
    fun getUserLoginRankAndFlow(rangeCondition: RangeCondition, topCount: Int): ApiResponse


    /**
     * 功能描述: 获取用户登录终端系统占比饼图数据
     * @auther changgq
     * @date 2018/5/31 16:12
     *
     * @param rangeCondition
     * @return
     */
    fun getTerminalPie(rangeCondition: RangeCondition): ApiResponse

    /**
     * 功能描述: 根据用户名获取登录总次数
     * @auther changgq
     * @date 2018/5/31 17:04
     *
     * @param condition
     * @param userName
     * @return
     */
    fun getUserLoginCount(condition: SearchCondition, userName: String): ApiResponse

    /**
     * 功能描述: 获取用户登录统计表格数据
     * @auther changgq
     * @date 2018/5/31 17:19
     *
     * @param condition
     * @return
     */
    fun getUserLoginGroupStatics(condition: Condition): ApiResponse

    /**
     * 功能描述: 获取用户登录统计表格【明细】数据
     * @auther changgq
     * @date 2018/5/31 17:19
     *
     * @param condition
     * @return
     */
    fun getUserLoginDetailsCount(condition: SearchCondition): ApiResponse
}