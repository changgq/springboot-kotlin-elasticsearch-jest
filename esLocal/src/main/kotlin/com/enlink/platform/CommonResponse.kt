package com.enlink.platform

data class CommonResponse(val data: Any?,
                          val response_time: Long = 0,
                          val status_code: Int = HttpStatus.OK,
                          val message: String = HttpStatus.responses.get(status_code)!!)

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