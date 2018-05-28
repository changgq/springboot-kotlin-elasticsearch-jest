package com.es.common

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.system.measureTimeMillis

@ControllerAdvice
class GlobalException {
    @ResponseBody
    @ExceptionHandler
    fun processException(e: Exception): ApiResponse {
        val elapsed_time_ = measureTimeMillis {}
        return ApiResponse(null, elapsed_time_, HTTP_Status.INTERNAL_SERVER_ERROR,
                "[" + HTTP_Status.responses[HTTP_Status.INTERNAL_SERVER_ERROR] + "]:" + e.message)
    }
}

class GlobalError(val error: ERROR) : Exception() {
    enum class ERROR(val m: String) {
        SYSTEM_ERROR("系统异常"),
        THE_REQUIRED_PARAMETERS_ARE_MISSING("存在必填参数缺失"),
        PARTIAL_PARAMETER_ERROR("部分参数格式错误"),
        PARAMETER_TYPE_NOT_CONFORMITY("请求的参数类型不符合要求"),
        NOT_RIGHT_ACCESS("无权限访问"),
        BODY_MISSING("请求数据的body缺失/类型不符合"),
        PAGING_PARAMETER_ERROR("分页参数出错"),
        GETTING_INFORMATION_FAILURE("获取信息失败"),
        LOG_SETTING_FAILED("日志设置失败")
    }
}