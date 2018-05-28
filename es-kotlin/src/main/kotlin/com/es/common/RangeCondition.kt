package com.es.common

import org.springframework.util.StringUtils
import java.text.SimpleDateFormat
import java.util.*

class RangeCondition(val type: Type, val conditionName: String, val gteValue: String, val lteValue: String, val timeZone: String = "+08:00") {
    enum class Type {
        DATE, NUMBER, STRING
    }

    fun selfCheck(): Boolean {
        if (null == type) {
            return false
        }
        if (StringUtils.isEmpty(conditionName)) {
            return false
        }
        if (StringUtils.isEmpty(gteValue)) {
            return false
        }
        if (StringUtils.isEmpty(lteValue)) {
            return false
        }
        if (type == Type.DATE) {
            if (!(isRightDateStr(gteValue, "yyyy-MM-dd") && isRightDateStr(lteValue, "yyyy-MM-dd"))) {
                return false
            }
        }
        return true
    }
}

fun isRightDateStr(dateStr: String, datePattern: String): Boolean {
    val dateFormat = SimpleDateFormat(datePattern)
    try {
        dateFormat.isLenient = false
        val date: Date = dateFormat.parse(dateStr)
        val newDateStr: String = dateFormat.format(date)
        return dateStr.equals(newDateStr)
    } catch (e: Exception) {
        return false
    }
}