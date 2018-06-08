package com.es.echarts

import com.es.common.ApiResponse
import com.es.common.GsonUtils
import java.util.*

/**
 * 功能描述:
 * @auther changgq
 * @date 2018/5/30 9:51
 *
 * @param
 * @return
 */
data class EchartsData(val xAxis_String: Array<String> = emptyArray(), val xAxis_Integer: Array<Int> = emptyArray(),
                  val xAxis_Float: Array<Float> = emptyArray(),
                  val yAxis_String: Array<String> = emptyArray(), val yAxis_Integer: Array<Int> = emptyArray(),
                  val yAxis_Float: Array<Float> = emptyArray())

fun main(args: Array<String>) {
    println(GsonUtils.convert(ApiResponse(EchartsData())))
}


data class ResourceLogVisit(val resourceName: String, val uri: String, val visitCount: Int, val lastVisitDate: String, val totalTraffic: String)