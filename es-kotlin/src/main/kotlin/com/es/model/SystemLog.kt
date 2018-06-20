package com.es.model

/**
 * 功能描述：系统日志
 *
 * @auther changgq
 * @date 2018/6/5 15:41
 * @description
 */
data class SystemLog(
        val fan: String = "",
        val cpu: String = "",
        val operateType: String = "",
        val systemMsg: String = "",
        val logTimeStamp: String = "") : BaseLog()