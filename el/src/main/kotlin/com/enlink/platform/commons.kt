package com.enlink.platform

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.util.zip.CRC32
import java.util.zip.CheckedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.apache.poi.ss.util.CellRangeAddress
import java.util.HashMap
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook


data class CommonResponse(val data: Any?,
                          val response_time: Long = 0,
                          val status_code: Int = HttpStatus.OK,
                          val message: String = HttpStatus.responses.get(status_code)!!)


/**
 * 功能描述: Gson工具类
 */
object GsonUtils {
    val gb: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()

    fun convert(any: Any): String {
        return gb.toJson(any)
    }

    fun <T> reConvert(json: String, clazz: Class<T>): T {
        return gb.fromJson(json, clazz)
    }

    fun <T> reConvert2List(json: String, type: Type): T {
        return gb.fromJson(json, type)
    }
}

/**
 * 功能描述: Excel工具类
 */
object ExcelUtils {
    enum class ExcelType {
        XLS, XLSX
    }

    fun genExcel(filePath: String,
                 headers: Array<String>,
                 datas: List<Array<String>>,
                 startIndex: Int = 0,
                 sheetName: String = "sheet1",
                 excelType: ExcelType = ExcelType.XLSX) {

        val f = File(filePath)
        val wb = when (excelType) {
            ExcelType.XLS -> if (f.exists()) HSSFWorkbook(f.inputStream()) else HSSFWorkbook()
            else -> if (f.exists()) XSSFWorkbook(f.inputStream()) else XSSFWorkbook()
        }

        
        var sheet = wb.getSheet(sheetName)
        if (null == sheet) sheet = wb.createSheet(sheetName)

        // 填写表头
        if (startIndex == 2) {
            // 创建第0行表头
            val row_0 = sheet.createRow(0)
            for (k in 0.rangeTo(headers.size - 1)) {
                row_0.createCell(k).setCellValue("")
            }
            // 创建第1行表头
            val row_1 = sheet.createRow(1)
            for (k in 0.rangeTo(headers.size - 1)) {
                row_1.createCell(k).setCellValue(headers[k])
            }
        }

        for (j in 0.rangeTo(datas.size - 1)) {
            val row = sheet.createRow(startIndex + j)
            for (k in 0.rangeTo(headers.size - 1)) {
                row.createCell(k).setCellValue(datas[j][k])
            }
        }
        wb.write(f.outputStream())
        wb.close()
    }

    /**
     * 此方法生成表头并写入表头名称
     *
     * @param nodes 节点
     * @param sheet 工作簿
     * @param style 单元格样式
     * @return 数据加载开始行
     */
//    fun generateHeader(nodes: List<HeaderNode>, sheet: Sheet, style: CellStyle): Int {
//        val hssfRowMap = HashMap<RowKey, Row>()
//        var dataStartRow = 0
//        for (node in nodes) {
//            if (!(node.firstRow == node.lastCol || node.firstCol == node.lastCol)) {
//                val cra = CellRangeAddress(node.firstRow, node.lastRow,
//                        node.firstCol, node.lastCol)
//                sheet.addMergedRegion(cra)
//            }
//            dataStartRow = if (dataStartRow >= node.lastRow) dataStartRow else node.lastRow
//            val key = RowKey()
//            key.setFirstRow(node.firstRow)
//            key.setLastRow(node.lastRow)
//            var row: Row? = hssfRowMap[key]
//            if (null == row) {
//                row = sheet.createRow(node.firstRow)
//                hssfRowMap[key] = row
//            }
//            val cell = row!!.createCell(node.firstCol)
//            cell.setCellValue(node.name)
//            cell.setCellStyle(style)
//        }
//        return dataStartRow + 1
//    }

    class HeaderNode {
        var name: String? = null
        var firstRow: Int = 0
        var lastRow: Int = 0
        var firstCol: Int = 0
        var lastCol: Int = 0
    }
}

object CommonStringUtils {
    fun hexStringToString(hexStr: String): String {
        val str = "0123456789ABCDEF"
        val hexs = hexStr.toCharArray()
        val bytes = ByteArray(hexStr.length / 2)
        var n: Int
        for (i in bytes.indices) {
            n = str.indexOf(hexs[2 * i]) * 16
            n += str.indexOf(hexs[2 * i + 1])
            bytes[i] = (n and 0xff).toByte()
        }
        return String(bytes, Charset.forName("UTF-8"))
    }
}

/**
 * 功能描述: Zip工具类
 */
class ZipCompressor(val zipPathName: String) {
    private val LOGGER: Logger = LoggerFactory.getLogger(ZipCompressor::class.java)
    private val BUFFER_SIZE = 8192
    private val zipFile: File

    init {
        zipFile = File(zipPathName)
    }

    /**
     * 功能描述: 将多个文件压缩进压缩包zipFile
     */
    fun compress(vararg filePaths: String) {
        if (!zipFile.exists()) {
            zipFile.createNewFile()
        }
        val zipOut = ZipOutputStream(CheckedOutputStream(zipFile.outputStream(), CRC32()))
        filePaths.forEach { it ->
            val f = File(it)
            LOGGER.info("Path==>" + f.path)
            compressFile(f, zipOut, "")
        }
        zipOut.close()
    }

    fun compressFile(f: File, zipOut: ZipOutputStream, baseDir: String) {
        if (f.exists()) {
            zipOut.putNextEntry(ZipEntry(baseDir + f.name))
            val buff = BufferedInputStream(f.inputStream())
            val data = ByteArray(BUFFER_SIZE)
            var isWrite = true
            while (isWrite) {
                val count = buff.read(data, 0, BUFFER_SIZE)
                if (count > 0) {
                    zipOut.write(data, 0, count)
                    continue
                }
                isWrite = false
            }
            buff.close()
        }
    }
}


/**
 * 功能描述: 索引映射关系
 */
object IndexMappings {
    val Index_mappings = mapOf<String, String>(
            "userLog" to "user",                    // 用户操作日志
            "systemLog" to "system",                // 系统日志
            "adminLog" to "admin",                  // 管理员操作日志
            "loginLog" to "login",                  // 登陆日志
            "resLog" to "res",                      // 资源访问日志
            "allLog" to "ALL",                      // 所有日志
            "customAllLog" to "ENLINK_CUSTOM_ALL"   // 所有日志
    )

    val Index_field_mappings_CN = mapOf<String, String>(
            "userName" to "用户名",
            "userGroup" to "用户组",
            "userAuth" to "用户权限",
            "date" to "时间",
            "visitAddress" to "访问地址",
            "ipAddress" to "ip地址",
            "macAddress" to "硬件地址",
            "operation" to "操作",
            "logInfo" to "日志信息",
            "linkInterface" to "链接隧道",
            "certificateServer" to "认证服务器",
            "status" to "登录状态",
            "logInfo" to "登录信息",
            "deviceType" to "登录设备类型",
            "deviceOS" to "登录设备操作系统",
            "clientInfo" to "客户端信息",
            "process" to "进程",
            "service" to "服务",
            "resourceName" to "资源名",
            "uri" to "uri地址",
            "uploadTraffic" to "上行流量",
            "downloadTraffic" to "下行流量",
            "totalTraffic" to "总流量",
            "visitType" to "访问类型",
            "fileName" to "文件名",
            "fileFormat" to "文件格式",
            "browserInfo" to "浏览器信息",
            "urlHttp" to "url/http协议"
    )

    val Index_field_mappings = mapOf<String, String>(
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

            "uplinkTraffic" to "long_uplink_traffic",
            "downlinkTraffic" to "long_downlink_traffic",

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

/**
 * 功能描述: HTTP状态码
 */
object HttpStatus {
    var OK = 200
    val CREATED = 201
    val ACCEPTED = 202
    val NON_AUTHORITATIVE_INFORMATION = 203
    val NO_CONTENT = 204
    val RESET_CONTENT = 205
    val PARTIAL_CONTENT = 206
    val MULTI_STATUS = 207
    val IM_USED = 226

    // redirection
    val MULTIPLE_CHOICES = 300
    val MOVED_PERMANENTLY = 301
    val FOUND = 302
    val SEE_OTHER = 303
    val NOT_MODIFIED = 304
    val USE_PROXY = 305
    val TEMPORARY_REDIRECT = 307

    // client error
    val BAD_REQUEST = 400
    val UNAUTHORIZED = 401
    val PAYMENT_REQUIRED = 402
    val FORBIDDEN = 403
    val NOT_FOUND = 404
    val METHOD_NOT_ALLOWED = 405
    val NOT_ACCEPTABLE = 406
    val PROXY_AUTHENTICATION_REQUIRED = 407
    val REQUEST_TIMEOUT = 408
    val CONFLICT = 409
    val GONE = 410
    val LENGTH_REQUIRED = 411
    val PRECONDITION_FAILED = 412
    val REQUEST_ENTITY_TOO_LARGE = 413
    val REQUEST_URI_TOO_LONG = 414
    val UNSUPPORTED_MEDIA_TYPE = 415
    val REQUESTED_RANGE_NOT_SATISFIABLE = 416
    val EXPECTATION_FAILED = 417
    val UNPROCESSABLE_ENTITY = 422
    val LOCKED = 423
    val FAILED_DEPENDENCY = 424
    val UPGRADE_REQUIRED = 426
    val PRECONDITION_REQUIRED = 428
    val TOO_MANY_REQUESTS = 429
    val REQUEST_HEADER_FIELDS_TOO_LARGE = 431

    // server error
    val INTERNAL_SERVER_ERROR = 500
    val NOT_IMPLEMENTED = 501
    val BAD_GATEWAY = 502
    val SERVICE_UNAVAILABLE = 503
    val GATEWAY_TIMEOUT = 504
    val HTTP_VERSION_NOT_SUPPORTED = 505
    val INSUFFICIENT_STORAGE = 507
    val NOT_EXTENDED = 510
    val NETWORK_AUTHENTICATION_REQUIRED = 511

    // system error
    val SYSTEM_ERROR = 900
    val THE_REQUIRED_PARAMETERS_ARE_MISSING = 901
    val PARTIAL_PARAMETER_ERROR = 902
    val PARAMETER_TYPE_NOT_CONFORMITY = 903
    val NOT_RIGHT_ACCESS = 904
    val BODY_MISSING = 905
    val PAGING_PARAMETER_ERROR = 906
    val GETTING_INFORMATION_FAILURE = 907
    val LOG_SETTING_FAILED = 908

    val responses = hashMapOf(
            100 to "Continue",
            101 to "Switching Protocols",

            200 to "OK",
            201 to "Created",
            202 to "Accepted",
            203 to "Non-Authoritative Information",
            204 to "No Content",
            205 to "Reset Content",
            206 to "Partial Content",

            300 to "Multiple Choices",
            301 to "Moved Permanently",
            302 to "Found",
            303 to "See Other",
            304 to "Not Modified",
            305 to "Use Proxy",
            306 to "(Unused)",
            307 to "Temporary Redirect",

            400 to "Bad Request",
            401 to "Unauthorized",
            402 to "Payment Required",
            403 to "Forbidden",
            404 to "Not Found",
            405 to "Method Not Allowed",
            406 to "Not Acceptable",
            407 to "Proxy Authentication Required",
            408 to "Request Timeout",
            409 to "Conflict",
            410 to "Gone",
            411 to "Length Required",
            412 to "Precondition Failed",
            413 to "Request Entity Too Large",
            414 to "Request-URI Too Long",
            415 to "Unsupported Media Type",
            416 to "Requested Range Not Satisfiable",
            417 to "Expectation Failed",
            428 to "Precondition Required",
            429 to "Too Many Requests",
            431 to "Request Header Fields Too Large",

            500 to "Internal Server Error",
            501 to "Not Implemented",
            502 to "Bad Gateway",
            503 to "Service Unavailable",
            504 to "Gateway Timeout",
            505 to "HTTP Version Not Supported",
            511 to "Network Authentication Required",

            900 to "System Error",
            901 to "The Required Parameters Are Missing",
            902 to "Partial Parameter Error",
            903 to "Parameter Type Not Conformity",
            904 to "Not Right Access",
            905 to "Body Missing",
            906 to "Paging Parameter Error",
            907 to "Getting Information Failure",
            908 to "Log Setting Failed"
    )
}