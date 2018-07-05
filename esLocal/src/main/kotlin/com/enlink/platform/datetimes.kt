package com.enlink.platform

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期操作类型
 */
enum class DateOptUnit {
    YEAR, MONTH, DATE;

    fun parseType(): Int {
        var value = Calendar.DATE
        when (this) {
            YEAR -> value = Calendar.DATE
            MONTH -> value = Calendar.MONTH
            DATE -> value = Calendar.DATE
        }
        return value
    }
}

/**
 * 日志操作类型对象
 */
data class DateOperator(val unit: DateOptUnit, val value: Int)

fun Any.year(value: Int): DateOperator {
    return DateOperator(DateOptUnit.YEAR, value)
}

fun Any.month(value: Int): DateOperator {
    return DateOperator(DateOptUnit.MONTH, value)
}

fun Any.day(value: Int): DateOperator {
    return DateOperator(DateOptUnit.DATE, value)
}

/**
 * date + year(3)
 * 往后的几天
 */
operator fun Date.plus(nextVal: DateOperator): Date {
    val calendar = DateTimeUtils.newCal
    calendar.time = this
    calendar.add(nextVal.unit.parseType(), nextVal.value)
    return calendar.time
}

/**
 * date - month(4)
 */
operator fun Date.minus(nextVal: DateOperator): Date {
    val calendar = DateTimeUtils.newCal
    calendar.time = this
    calendar.add(nextVal.unit.parseType(), nextVal.value * -1)
    return calendar.time
}

/**
 * 得到月末
 */
operator fun Date.inc(): Date {
    val calendar = DateTimeUtils.newCal
    calendar.time = this
    calendar.add(Calendar.MONTH, 1);
    calendar.set(Calendar.DAY_OF_MONTH, 0);
    return calendar.time
}

/**
 * 得到月初
 */
operator fun Date.dec(): Date {
    val calendar = DateTimeUtils.newCal
    calendar.time = this
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    return calendar.time
}

/**
 * 取 年月日时分秒 0 - 5
 * 例如 2015-12-21 22:15:56
 * date[0]:2015  date[1]:12 date[2]:21
 */
operator fun Date.get(position: Int): Int {
    val calendar = DateTimeUtils.newCal
    calendar.time = this
    var value = 0
    when (position) {
        0 -> value = calendar.get(Calendar.YEAR)
        1 -> value = calendar.get(Calendar.MONTH) + 1
        2 -> value = calendar.get(Calendar.DAY_OF_MONTH)
        3 -> value = calendar.get(Calendar.HOUR)
        4 -> value = calendar.get(Calendar.MINUTE)
        5 -> value = calendar.get(Calendar.SECOND)
    }
    return value
}

/**
 * 比较2个日期
 * if(date1 > date2) {}
 */
operator fun Date.compareTo(compareDate: Date): Int {
    return (time - compareDate.time).toInt()
}

/**
 * 日期转化为字符串
 */
fun Date.stringFormat(formatType: String): String {
    return SimpleDateFormat(formatType).format(this)
}

inline fun Date.date2string_point(): String {
    return SimpleDateFormat("yyyy.MM.dd").format(this)
}

inline fun Date.date2string(): String {
    return SimpleDateFormat("yyyy-MM-dd").format(this)
}

inline fun Date.datetime2string(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this)
}

inline fun Date.datetime2filename(): String {
    return SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(this)
}

inline fun String.string2date(): Date {
    return SimpleDateFormat("yyyy-MM-dd").parse(this)
}

inline fun String.string2datetime(format: String = "yyyy-MM-dd HH:mm:ss"): Date {
    return SimpleDateFormat(format).parse(this)
}

inline fun String.checkIsDate(): Boolean {
    try {
        return this.string2date().date2string() == this
    } catch (e: Exception) {
        return false
    }
}

object DateTimeUtils {
    val newCal = GregorianCalendar()

    fun currentWeekFirst(): Date {
        var cal = newCal
        cal.time = Date()
        cal.set(Calendar.DAY_OF_WEEK, 1)
        return cal.time
    }

    fun currentWeekLast(): Date {
        var cal = newCal
        cal.time = Date()
        cal.set(Calendar.DAY_OF_WEEK, 1)
        cal.roll(Calendar.DAY_OF_WEEK, -1)
        return cal.time
    }

    fun currentMonthFirst(): Date {
        var cal = newCal
        cal.time = Date()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.time
    }

    fun currentMonthLast(): Date {
        var cal = newCal
        cal.time = Date()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.roll(Calendar.DAY_OF_MONTH, -1)
        return cal.time
    }

    fun currentYearFirst(): Date {
        var cal = newCal
        cal.time = Date()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        return cal.time
    }

    fun currentYearLast(): Date {
        var cal = newCal
        cal.time = Date()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.roll(Calendar.DAY_OF_YEAR, -1)
        return cal.time
    }

    fun last3monthsFirst(): Date {
        var cal = newCal
        cal.time = Date()
        cal.add(Calendar.DATE, 90 * -1)
        return cal.time
    }

    fun last6monthsFirst(): Date {
        var cal = newCal
        cal.time = Date()
        cal.add(Calendar.DATE, 180 * -1)
        return cal.time
    }

    fun last12monthsFirst(): Date {
        var cal = newCal
        cal.time = Date()
        cal.add(Calendar.DATE, 365 * -1)
        return cal.time
    }

    fun string2Datetime(datetime: String, format: String = "yyyy-MM-dd HH:mm:ss") {

    }
}

