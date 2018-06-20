package com.es.common

import org.elasticsearch.search.sort.SortOrder
import java.io.Serializable

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/11 14:37
 * @description
 */
class Condition(
        val preciseConditions: Map<String, Array<String>> = emptyMap(),
        val ambiguousConditions: Map<String, String> = emptyMap(),
        val rangeConditionList: List<RangeCondition> = emptyList(),
        val sortConditions: Map<String, SortOrder> = emptyMap(),
        val currentPage: Int = 1,
        val pageSize: Int = 10,
        val scrollId: String = ""
) : Serializable