package com.es.common

import org.elasticsearch.search.sort.SortOrder
import java.io.Serializable


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
        val sortConds: Map<String, SortOrder>? = emptyMap(),
        var currentPage: Int = 1,
        var pageSize: Int = 10,
        val scrollId: String = ""
) : Serializable {
    // 精确查询条件
    val exactList: List<QueryCondition>
    // 混合查询条件
    val dimList: List<QueryCondition>
    // 范围查询条件
    val scopeList: List<RangeCondition>
    // 排序条件
    val sortList: List<SortCondition>

    init {
        if (currentPage <= 0) currentPage = 1
        if (pageSize <= 0) pageSize = 10
        exactList = initExactList()
        dimList = initDimList()
        scopeList = initScopeList()
        sortList = initSortList()
    }

    private fun initExactList(): List<QueryCondition> {
        return queryConds!!.filter { x -> x.queryType == QueryCondition.QueryType.EXACT }
    }

    private fun initDimList(): List<QueryCondition> {
        return queryConds!!.filter { x -> x.queryType == QueryCondition.QueryType.DIM }
    }

    private fun initScopeList(): List<RangeCondition> {
        return rangeQueryCons!!
    }

    private fun initSortList(): List<SortCondition> {
        return List<SortCondition>(sortConds!!.keys.size, { it ->
            val k = sortConds!!.keys.elementAt(it)
            SortCondition(k, sortConds[k]!!)
        })
    }
}