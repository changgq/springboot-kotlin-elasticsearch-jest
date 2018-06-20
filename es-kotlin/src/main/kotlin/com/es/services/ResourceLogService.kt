package com.es.services

import com.es.common.ApiResponse
import com.es.common.Condition
import com.es.common.RangeCondition
import com.es.common.SearchCondition
import org.springframework.web.bind.annotation.RequestBody

/**
 * 功能描述：资源日志业务层接口
 *
 * @auther changgq
 * @date 2018/5/30 10:26
 * @description
 */
interface ResourceLogService {
    fun resourceVisitRank(rangeCons: RangeCondition, topCount: Int = 10, sortBy: Boolean = false): ApiResponse
    fun resourceOrderByDownload(rangeCons: RangeCondition, topCount: Int = 10, sortBy: Boolean = false): ApiResponse
    fun resourceOrderByType(rangeCons: RangeCondition, topCount: Int = 10, sortBy: Boolean = false): ApiResponse
    fun resTotalCount(cons: RangeCondition): ApiResponse
    fun applicationPie(rangeCondition: RangeCondition): ApiResponse
    fun linkTrafficRank(rangeCondition: RangeCondition, topCount: Int, type: String? = "down"): ApiResponse
    fun fileFormatPie(rangeCondition: RangeCondition, type: String?): ApiResponse
    // 暂时无法实现，使用原方法会导致集群宕机
    fun resourceVisitGroupStatics(searchCondition: Condition): ApiResponse

    // 暂时无法实现，使用原方法会导致集群宕机
    fun resourceVisitDetails(searchCondition: SearchCondition): ApiResponse

    fun getTrafficRank(rangeCondition: RangeCondition, topCount: Int, type: String): ApiResponse
    fun getUserFlowAndLoginCount(rangeCondition: RangeCondition, topCount: Int): ApiResponse
    fun getTrafficPie(rangeCondition: RangeCondition, type: String): ApiResponse
    fun getTrafficGroupStatics(condition: SearchCondition, type: String): ApiResponse
    fun getTrafficDetails(condition: SearchCondition, type: String): ApiResponse
}