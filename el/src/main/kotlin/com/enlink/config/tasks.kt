package com.enlink.config

import com.enlink.platform.date2string_point
import org.apache.lucene.queryparser.xml.QueryBuilder
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.reindex.ReindexRequest
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/22 11:11
 * @description
 */
//@Component
open class DeleteTask {
    val LOGGER = LoggerFactory.getLogger(this::class.java)
    @Autowired
    lateinit var client: RestHighLevelClient

    @Async
    @Scheduled(cron = "0 0/1 * * * *")
    open fun run() {
        LOGGER.info("定时删除任务开始执行......")
    }
}

//@Component
open class ReIndexTask {
    val LOGGER = LoggerFactory.getLogger(this::class.java)
    @Autowired
    lateinit var client: RestHighLevelClient

    @Async
    @Scheduled(cron = "0 0/1 * * * *")
    open fun run() {
        LOGGER.info("重新索引任务开始执行......")
        val date = Date().date2string_point()
        val rr: ReindexRequest = ReindexRequest(SearchRequest(), IndexRequest("res-$date"))
//        client.indices().rollover()
        val _request = SearchRequest("res")
                .source(SearchSourceBuilder()
                        .fetchSource(false)
                        .aggregation(AggregationBuilders
                                .terms("dates")
                                .field("@timestamp")
                                .format("yyyy-MM-dd HH:mm:ss").size(Int.MAX_VALUE)))
        LOGGER.info(_request.source().toString())
        val bk = client.search(_request).aggregations.get<Terms>("dates").buckets


    }
}