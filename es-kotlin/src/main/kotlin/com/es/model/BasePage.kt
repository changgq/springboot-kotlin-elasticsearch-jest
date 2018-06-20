package com.es.model

/**
 * 功能描述：分页
 *
 * @auther changgq
 * @date 2018/5/30 17:51
 * @description
 */
data class BasePage(var currentPage: Int = 1, var pageSize: Int = 10, var scrollId: String = "",
                    var totalHits: Long = 0, var data: List<Any>? = emptyList(), var pageCount: Int = 1) {
    init {
        if (currentPage <= 0) currentPage = 1
        if (pageSize <= 0) pageSize = 10

        totalHits = if (totalHits > 10000) 10000 else totalHits

        val _pCount = (totalHits / pageSize).toInt()
        pageCount = if (_pCount > 1000) 999 else _pCount
    }
}