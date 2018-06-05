package com.es.common;

/**
 * 功能描述：查询条件
 *
 * @auther changgq
 * @date 2018/6/1 09:55
 * @description
 */
class QueryCondition(val queryName: String, val queryValue: Any?, val queryType: QueryType) {
    enum class QueryType {
        EXACT, DIM
    }
}