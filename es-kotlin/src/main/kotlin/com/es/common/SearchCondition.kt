package com.es.common

import javax.management.Query

/**
 * 功能描述：查询条件
 *
 * @auther changgq
 * @date 2018/5/30 13:23
 * @description
 */
class SearchCondition(
        val rangeQueryCons: List<RangeCondition>? = emptyList(),
        val queryConds: List<QueryCondition>? = emptyList(),
        var currentPage: Int = 1, var pageSize: Int = 50, val scrollId: String = ""
) {
    // 精确查询条件
    val exactList: List<QueryCondition>
    // 混合查询条件
    val dimList: List<QueryCondition>
    // 范围查询条件
    val scopeList: List<RangeCondition>

    init {
        if (currentPage <= 0) currentPage = 1
        if (pageSize <= 0) pageSize = 10
        exactList = initExactList()
        dimList = initDimList()
        scopeList = initScopeList()
    }

    private fun initExactList(): List<QueryCondition> {
        val _exactList: List<QueryCondition> = emptyList()
        queryConds!!.forEach { it ->
            if (it.queryType.equals(QueryCondition.QueryType.EXACT)) {
                _exactList.plus(it)
            }
        }
        return _exactList
    }

    private fun initDimList(): List<QueryCondition> {
        val _dimList: List<QueryCondition> = emptyList()
        queryConds!!.forEach { it ->
            if (it.queryType.equals(QueryCondition.QueryType.DIM)) {
                _dimList.plus(it)
            }
        }
        return _dimList
    }

    private fun initScopeList(): List<RangeCondition> {
        val _scopeList: List<RangeCondition> = emptyList()
        rangeQueryCons!!.forEach { it ->
            _scopeList.plus(it)
        }
        return _scopeList
    }
}