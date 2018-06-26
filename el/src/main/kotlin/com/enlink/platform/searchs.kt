package com.enlink.platform

import org.elasticsearch.search.sort.SortOrder
import java.io.Serializable
import java.util.*

/**
 * 功能描述：查询
 * @auther changgq
 */
class Condition(
        val preciseConditions: Map<String, Array<String>> = emptyMap(),
        val ambiguousConditions: Map<String, String> = emptyMap(),
        val rangeConditionList: List<RangeCondition> = emptyList(),
        val sortConditions: Map<String, SortOrder> = emptyMap(),
        val currentPage: Int = 1,
        val pageSize: Int = 10,
        val scrollId: String = ""
) : Serializable

class DownloadCondition(
        val preciseConditions: Map<String, Array<String>> = emptyMap(),
        val ambiguousConditions: Map<String, String> = emptyMap(),
        val rangeConditionList: List<RangeCondition> = emptyList(),
        val logType: String
) : Serializable

/**
 * 功能描述：范围查询条件
 * @auther changgq
 */
class RangeCondition(val type: Type = Type.DATE, var choice: String = "", val conditionName: String = "date",
                     var gteValue: String = "", var lteValue: String = "", val timeZone: String = "+08:00") {
    init {
        when (choice) {
            "week" -> { // 当前周
                gteValue = DateTimeUtils.currentWeekFirst().date2string()
                lteValue = DateTimeUtils.currentWeekLast().date2string()
            }
            "month" -> { // 最近一月
                gteValue = DateTimeUtils.currentMonthFirst().date2string()
                lteValue = DateTimeUtils.currentMonthLast().date2string()
            }
            "3months" -> { // 最近三个月
                gteValue = DateTimeUtils.last3monthsFirst().date2string()
                lteValue = Date().date2string()
            }
            "6months" -> { // 最近六个月
                gteValue = DateTimeUtils.last6monthsFirst().date2string()
                lteValue = Date().date2string()
            }
            "12months" -> { // 最近一年
                gteValue = DateTimeUtils.last12monthsFirst().date2string()
                lteValue = Date().date2string()
            }
            "year" -> { // 当前整年
                gteValue = DateTimeUtils.currentYearFirst().date2string()
                lteValue = DateTimeUtils.currentYearLast().date2string()
            }
            "day" -> { // 当前整年
                gteValue = Date().date2string()
                lteValue = Date().date2string()
            }
            else -> {
                gteValue = gteValue
                lteValue = lteValue
            }
        }
    }

    enum class Type {
        DATE, NUMBER, STRING
    }
}

class Pager(var currentPage: Int = 1, var pageSize: Int = 10, var totalHits: Long = 0,
            var pageDatas: List<Any>? = emptyList(), var scrollId: String? = "", var pageCount: Int = 1) {
    init {
        if (currentPage <= 0) currentPage = 1
        if (pageSize <= 0) pageSize = 10
        if (totalHits > 10000) totalHits = 10000
        pageCount = (totalHits / pageSize).toInt()
    }
}