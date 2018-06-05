package com.es.services

import com.es.common.ApiResponse

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/1 14:49
 * @description
 */
interface SystemLogService {
    fun getNowNetFlow(): ApiResponse
    fun getNetFlowList(): ApiResponse
}