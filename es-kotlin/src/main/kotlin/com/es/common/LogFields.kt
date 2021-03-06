package com.es.common

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/12 14:41
 * @description
 */
object LogFields {
    val LOG_INDEX_MAP = mapOf<String, String>(
            "userLog" to "user",                    // 用户操作日志
            "systemLog" to "system",                // 系统日志
            "adminLog" to "admin",                  // 管理员操作日志
            "loginLog" to "login",                  // 登陆日志
            "resLog" to "res",                      // 资源访问日志
            "allLog" to "ALL",                      // 所有日志
            "customAllLog" to "ENLINK_CUSTOM_ALL"   // 所有日志
    )

    val FIELD_MAP = mapOf<String, String>(
            "resourceName" to "resource_name",
            "resourceName.keyword" to "resource_name.keyword",

            //uri
            "uri" to "uri",
            "uri.keyword" to "uri.keyword",

            //访问地址
            "visitAddress" to "visit_address",
            "visitAddress.keyword" to "visit_address.keyword",

            //浏览器信息
            "browserInfo" to "browser_info",
            "browserInfo.keyword" to "browser_info.keyword",

            //url/http协议
            "urlHttp" to "url_http",
            "urlHttp.keyword" to "urlHttp.keyword",

            //日志级别
            "logLevel" to "keyword_log_level",
            "logLevel.keyword" to "keyword_log_level",

            //接收时间
            "date" to "@timestamp",

            //完整日志信息
            "message" to "message",
            "message.keyword" to "message.keyword",

            //用户id（resLog实际实现时，是表示用户名)
            "userId" to "user_id",
            "userId.keyword" to "user_id.keyword",

            //用户名(resLog实际实现时，是表示用户全名)
            "userName" to "user_name",
            "userName.keyword" to "user_name.keyword",

            //用户组
            "userGroup" to "user_group",
            "userGroup.keyword" to "user_group.keyword",

            //用户权限
            "userAuth.keyword" to "keyword_user_auth",
            "userAuth" to "keyword_user_auth",

            //操作
            "operation" to "operation",
            "operation.keyword" to "operation.keyword",

            //ip地址
            "ipAddress" to "ip_address",
            "ipAddress.keyword" to "ip_address.keyword",

            //mac地址
            "macAddress" to "mac_address",
            "macAddress.keyword" to "mac_address.keyword",

            //备注信息
            "logInfo" to "log_info",
            "logInfo.keyword" to "log_info.keyword",

            //日志时间
            "logTimeStamp" to "log_timestamp",
            "logTimeStamp.keyword" to "log_timestamp.keyword",

            //认证服务器
            "certificateServer" to "certificate_server",
            "certificateServer.keyword" to "certificate_server.keyword",

            //链接隧道
            "linkInterface" to "link_interface",
            "linkInterface.keyword" to "link_interface.keyword",

            //服务
            "service" to "service",
            "service.keyword" to "service.keyword",

            //进程
            "process" to "process",
            "process.keyword" to "process.keyword",

            //登陆设备操作系统
            "deviceOS" to "device_os",
            "deviceOS.keyword" to "device_os.keyword",

            //登陆设备类型（windows客户端or网页、ios app、andriod app、macos app）
            "deviceType" to "device_os",
            "deviceType.keyword" to "device_os.keyword",

            //登陆客户端版本
            "clientInfo" to "client_info",
            "clientInfo.keyword" to "client_info.keyword",

            //操作状态
            "status" to "keyword_status",
            "status.keyword" to "keyword_status",

            "totalTraffic" to "long_total_traffic",
            "uploadTraffic" to "long_uplink_traffic",
            "downloadTraffic" to "long_downlink_traffic",

            "visitType" to "keyword_visit_type",
            "visitType.keyword" to "keyword_visit_type",

            "fileName" to "file_name",
            "fileName.keyword" to "file_name",

            "fileFormat" to "keyword_file_format",
            "fileFormat.keyword" to "keyword_file_format",

            "appType" to "app_type",
            "appType.keyword" to "app_type.keyword"
    )
}