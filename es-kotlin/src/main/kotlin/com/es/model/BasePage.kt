package com.es.model

/**
 * 功能描述：分页
 *
 * @auther changgq
 * @date 2018/5/30 17:51
 * @description
 */
data class BasePage(var pageIndex: Int = 1, var pageSize: Int = 10, var scrollId: String = "",
                    var total: Long = 0, var data: List<Any>? = emptyList()) {
    init {
        if (pageIndex <= 0) pageIndex = 1
        if (pageSize <= 0) pageSize = 10
    }
}