package com.es.model

/**
 * 功能描述：管理员操作日志
 *
 * @auther changgq
 * @date 2018/6/5 15:45
 * @description
 */
data class AdministratorOperationLog(
        val userId: String = "",
        val userGroup: String = "",
        val userName: String = "",
        val userAuth: String = "",
        val operation: String = "",
        val ipAddress: String = "",
        val macAddress: String = "",
        val logInfo: String = "",
        val logTimeStamp: String = ""
) : BaseLog()