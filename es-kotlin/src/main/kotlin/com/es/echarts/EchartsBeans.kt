package com.es.echarts

import java.util.*

/**
 * 功能描述:
 * @auther changgq
 * @date 2018/5/30 9:51
 *
 * @param
 * @return
 */
class EchartsData(val xAxis_String: Array<String> = emptyArray(), val xAxis_Integer: Array<Int> = emptyArray(),
                  val xAxis_Float: Array<Float> = emptyArray(),
                  val yAxis_String: Array<String> = emptyArray(), val yAxis_Integer: Array<Int> = emptyArray(),
                  val yAxis_Float: Array<Float> = emptyArray())


data class ResourceLogVisit(val resourceName: String, val uri: String, val visitCount: Int, val lastVisitDate: String, val totalTraffic: String)