package com.es.common

import com.es.date2string
import java.util.*


class RangeCondition(val type: Type = Type.DATE, choice: String = "day", val conditionName: String = "date",
                     var gteValue: String = "", var lteValue: String = "", val timeZone: String = "+08:00") {
    init {
        when (choice) {
            "week" -> { // 当前周
                gteValue = TimeUtils.currentWeekFirst().date2string()
                lteValue = TimeUtils.currentWeekLast().date2string()
            }
            "month" -> { // 最近一月
                gteValue = TimeUtils.currentMonthFirst().date2string()
                lteValue = TimeUtils.currentMonthLast().date2string()
            }
            "3months" -> { // 最近三个月
                gteValue = TimeUtils.last3monthsFirst().date2string()
                lteValue = Date().date2string()
            }
            "6months" -> { // 最近六个月
                gteValue = TimeUtils.last6monthsFirst().date2string()
                lteValue = Date().date2string()
            }
            "12months" -> { // 最近一年
                gteValue = TimeUtils.last12monthsFirst().date2string()
                lteValue = Date().date2string()
            }
            "year" -> { // 当前整年
                gteValue = TimeUtils.currentYearFirst().date2string()
                lteValue = TimeUtils.currentYearLast().date2string()
            }
            else -> {
                gteValue = Date().date2string()
                lteValue = Date().date2string()
            }
        }
    }

    enum class Type {
        DATE, NUMBER, STRING
    }
}



