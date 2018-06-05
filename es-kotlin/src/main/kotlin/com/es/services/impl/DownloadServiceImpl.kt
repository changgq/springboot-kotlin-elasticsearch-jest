package com.es.services.impl

import com.es.common.ExcelUtils
import com.es.common.SearchCondition
import com.es.model.ModelContants
import com.es.model.BasePage
import com.es.services.DownloadService
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/4 16:37
 * @description
 */
@Service
class DownloadServiceImpl(val highLevelClient: RestHighLevelClient) : DownloadService {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)

    fun download(searchCondition: SearchCondition): Array<String> {
        var searchPager = BasePage(searchCondition.currentPage, searchCondition.pageSize, "", 0)

        var loginCount = 0L
        var data: List<Map<String, Any>> = emptyList()
        val elapsed_time_ = measureTimeMillis {
            val bq = QueryBuilders.boolQuery()
            searchCondition.exactList!!.forEach { it ->
                bq.must(QueryBuilders.matchQuery(ModelContants.PARAMS_MAPS.get(it.queryName), it.queryValue))
            }
            searchCondition.dimList!!.forEach { it ->
                bq.must(QueryBuilders.termsQuery(ModelContants.PARAMS_MAPS.get(it.queryName), it.queryValue))
            }
            searchCondition.rangeQueryCons!!.forEach { it ->
                bq.must(QueryBuilders.rangeQuery(ModelContants.PARAMS_MAPS.get(it.conditionName))
                        .gte(it.gteValue).lte(it.lteValue).timeZone(it.timeZone))
            }
            SearchRequest().source(SearchSourceBuilder.searchSource().fetchSource(false).query(bq))
        }
        return arrayOf("")

        val headers: Array<String> = emptyArray()
        val datas: List<Array<String>> = emptyList()
        val wb = HSSFWorkbook()
        ExcelUtils.genExcelAddContent(HSSFWorkbook(), File("d:/a.xls"), headers, datas, 0)
        wb.close()
    }
}