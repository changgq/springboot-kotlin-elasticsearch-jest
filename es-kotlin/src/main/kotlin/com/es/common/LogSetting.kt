package com.es.common

import com.es.date2string
import java.util.*


/**
 * 功能描述：日志设置功能
 *
 * @auther changgq
 * @date 2018/6/4 09:32
 * @description
 */
class LogSetting(val useThirdDB: Boolean = false, // 是否启用第三方数据库
                 val configType: ConfigType = ConfigType.DATE,  // 日志清理类型，按日期、按数量
                 val startSaveTime: String = Date().date2string(), // 日志设置的开始时间（yyyy-MM-dd日期格式）
                 val dayRate: Long = 90, // 时间周期
                 val countRate: Long = 0 // 条数周期
) {
    enum class ConfigType {
        DATE, COUNT
    }
}