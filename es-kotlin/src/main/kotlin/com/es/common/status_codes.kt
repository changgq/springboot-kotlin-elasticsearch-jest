package com.enlink.eslogs.common

enum class StatusCodes(
        val code: Int, val _name: String, val message: String
) {
    CONTINUE(100, "CONTINUE", "OK"),
    SWITCHING_PROTOCOLS(101, "SWITCHING_PROTOCOLS", "Created"),

    OK(200, "OK", "OK"),
    CREATED(201, "CREATED", "Created"),
    ACCEPTED(202, "ACCEPTED", "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "NON_AUTHORITATIVE_INFORMATION", "Non-Authoritative Information"),
    NO_CONTENT(204, "NO_CONTENT", "No Content"),
    RESET_CONTENT(205, "RESET_CONTENT", "Reset Content"),
    PARTIAL_CONTENT(206, "PARTIAL_CONTENT", "Partial Content"),
    MULTI_STATUS(207, "MULTI_STATUS", "Muliti Status"),
    IM_USED(226, "IM_USED", "Im Used"),

    // redirection
    MULTIPLE_CHOICES(300, "MULTIPLE_CHOICES", "Multiple Choices"),
    MOVED_PERMANENTLY(301, "MOVED_PERMANENTLY", "Moved Permanently"),
    FOUND(302, "FOUND", "Found"),
    SEE_OTHER(303, "SEE_OTHER", "See Other"),
    NOT_MODIFIED(304, "NOT_MODIFIED", "Not Modified"),
    USE_PROXY(305, "USE_PROXY", "Use Proxy"),
    UNUSED(306, "UNUSED", "(Unused)"),
    TEMPORARY_REDIRECT(307, "TEMPORARY_REDIRECT", "Temporary Redirect"),

    // client error
    BAD_REQUEST(400, "BAD_REQUEST", "Bad Request"),
    UNAUTHORIZED(401, "UNAUTHORIZED", "Unauthorized"),
    PAYMENT_REQUIRED(402, "PAYMENT_REQUIRED", "Payment Required"),
    FORBIDDEN(403, "FORBIDDEN", "Forbidden"),
    NOT_FOUND(404, "NOT_FOUND", "Not Found"),
    METHOD_NOT_ALLOWED(405, "METHOD_NOT_ALLOWED", "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "NOT_ACCEPTABLE", "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "PROXY_AUTHENTICATION_REQUIRED", "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "REQUEST_TIMEOUT", "Request Timeout"),
    CONFLICT(409, "CONFLICT", "Conflict"),
    GONE(410, "GONE", "Gone"),
    LENGTH_REQUIRED(411, "LENGTH_REQUIRED", "Length Required"),
    PRECONDITION_FAILED(412, "PRECONDITION_FAILED", "Precondition Failed"),
    REQUEST_ENTITY_TOO_LARGE(413, "REQUEST_ENTITY_TOO_LARGE", "Request Entity Too Large"),
    REQUEST_URI_TOO_LONG(414, "REQUEST_URI_TOO_LONG", "Request-URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "UNSUPPORTED_MEDIA_TYPE", "Unsupported Media Type"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "REQUESTED_RANGE_NOT_SATISFIABLE", "Requested Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "EXPECTATION_FAILED", "Expectation Failed"),
    UNPROCESSABLE_ENTITY(422, "UNPROCESSABLE_ENTITY", "Unprocessable Entity"),
    LOCKED(423, "LOCKED", "Locked"),
    FAILED_DEPENDENCY(424, "FAILED_DEPENDENCY", "Failed Dependency"),
    UPGRADE_REQUIRED(426, "UPGRADE_REQUIRED", "Upgrade Required"),
    PRECONDITION_REQUIRED(428, "PRECONDITION_REQUIRED", "Precondition Required"),
    TOO_MANY_REQUESTS(429, "TOO_MANY_REQUESTS", "Too Many Requests"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "REQUEST_HEADER_FIELDS_TOO_LARGE", "Request Header Fields Too Large"),

    // server error
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "Internal Server Error"),
    NOT_IMPLEMENTED(501, "NOT_IMPLEMENTED", "Not Implemented"),
    BAD_GATEWAY(502, "BAD_GATEWAY", "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "SERVICE_UNAVAILABLE", "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "GATEWAY_TIMEOUT", "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP_VERSION_NOT_SUPPORTED", "HTTP Version Not Supported"),
    INSUFFICIENT_STORAGE(507, "INSUFFICIENT_STORAGE", "Insufficient Storage"),
    NOT_EXTENDED(510, "NOT_EXTENDED", "Not Extended"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "NETWORK_AUTHENTICATION_REQUIRED", "Network Authentication Required");
}

fun main(args: Array<String>) {
    for (v in StatusCodes.values()) {
        println(v)
    }
    println(StatusCodes.valueOf("StatusCodes"));
}