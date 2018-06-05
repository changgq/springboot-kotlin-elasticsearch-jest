package com.es.model

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/5 15:37
 * @description
 */
data class LoginLog(
        val deviceType: String = "",
        val deviceOS: String = "",
        val clientInfo: String = "",
        val operation: String = "",
        val status: String = "",
        val userId: String = "",
        val userGroup: String = "",
        val userName: String = "",
        val userAuth: String = "",
        val certificateServer: String = "",
        val linkInterface: String = "",
        val macAddress: String = "",
        val ipAddress: String = ""
) : BaseLog() {

}