package com.es.model

/**
 * 功能描述：用户操作日志
 *
 * @auther changgq
 * @date 2018/6/5 15:43
 * @description
 */
data class UserOperationLog(
        var userId: String = "",
        val userGroup: String = "",
        val userName: String = "",
        val userAuth: String = "",
        val operation: String = "",
        val ipAddress: String = "",
        val macAddress: String = "",
        val logInfo: String = "",
        val logTimeStamp: String = "",
        val certificateServer: String = "",
        val linkInterface: String = ""
) : BaseLog()