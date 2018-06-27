package com.enlink.platform

import org.apache.http.HttpHost
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.*


object Excel2007Utils {
    var rowaccess: Int = 1000// 内存中缓存记录行数，以免内存溢出

    fun createExcel(filePath: String, headers: Array<String>, dataArrays: List<Array<Any>>) {
        println("开始写入文件.......")
        val expose_time = measureTimeMillis {
            var xwb: SXSSFWorkbook = SXSSFWorkbook(10000)
            var startIndex : Int = 0
            val f = File(filePath)
            if (f.exists()) {
                val wb = XSSFWorkbook(f.inputStream())
                startIndex = wb.getSheet("sheet1").lastRowNum
                xwb = SXSSFWorkbook(wb, 10000)
            } else {
                f.parentFile.mkdirs()
            }
            var sh = xwb.getSheet("sheet1")
            if (null == sh) sh = xwb.createSheet("sheet1")
            println("startIndex ======================================== $startIndex")
            for (j in 0.rangeTo(dataArrays.size - 1)) {
                val row = sh.createRow(startIndex + 1 + j)
                val d = dataArrays[j]
                for (k in 0.rangeTo(d.size - 1)) {
                    row.createCell(k).setCellValue(d[k].toString())
                }
            }
            xwb.write(f.outputStream())
            xwb.close()
        }
        println("文件生成完成，耗时：$expose_time ms")
    }
}

object Excel2003Utils {

    fun createExcel(filePath: String, headers: Array<Any>, dataArrays: List<Array<Any>>) {
        val cvsFile = File(filePath)
        if (!cvsFile.exists()) {
            cvsFile.parentFile.mkdirs()
            cvsFile.createNewFile()
        }
        val cvsWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(cvsFile), "GB2312"), 1024)
        // 写入文件头部
        writeRow(headers, cvsWriter)
        // 写入文件内容
        for (row in dataArrays) {
            writeRow(row, cvsWriter)
        }
        cvsWriter.flush()
    }

    /**
     * 写一行数据方法
     * @param row
     * @param csvWriter
     * @throws IOException
     */
    fun writeRow(row: Array<Any>, csvWriter: BufferedWriter) {
        // 写入文件头部
        for (data in row) {
            val sb = StringBuffer()
            val rowStr = sb.append("\"").append(data).append("\",").toString()
            csvWriter.write(rowStr)
        }
        csvWriter.newLine()
    }
}


object CvsExportUtils {

    fun createExcel(filePath: String, headers: Array<String>, dataArrays: List<Array<String>>) {

    }
}

object CvsReaderUtils {
    fun createCvs(filePath: String, headers: Array<Any>, dataArrays: MutableList<Array<Any>>) {
        println("开始写入文件.......")
        val expose_time = measureTimeMillis {
            val cvsFile = File(filePath)
            if (!cvsFile.exists()) {
                cvsFile.parentFile.mkdirs()
                cvsFile.createNewFile()
            }
            val cvsWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(cvsFile, true), "GB2312"), 1024)
            // 写入文件头部
            writeRow(headers, cvsWriter)
            // 写入文件内容
            for (row in dataArrays) {
                writeRow(row, cvsWriter)
            }
            cvsWriter.flush()
        }
        println("文件生成完成，耗时：$expose_time ms")
    }

    /**
     * 写一行数据方法
     * @param row
     * @param csvWriter
     * @throws IOException
     */
    fun writeRow(row: Array<Any>, csvWriter: BufferedWriter) {
        // 写入文件头部
        for (data in row) {
            val sb = StringBuffer()
            val rowStr = sb.append("\"").append(data).append("\",").toString()
            csvWriter.write(rowStr)
        }
        csvWriter.newLine()
    }
}


fun dsss(args: Array<String>) {
    val client = RestHighLevelClient(RestClient.builder(HttpHost.create("http://49.4.9.225:9200")).setMaxRetryTimeoutMillis(120000))
    val expose_time_ = measureTimeMillis {
        val f = File("D:\\Test\\backups\\res-2018.03.03.xlsx")
        val xwb: SXSSFWorkbook = SXSSFWorkbook(10000)
        val sheet = xwb.createSheet("sheet1")
        var startIndex : Int = 0
        var response = client.search(SearchRequest("res-2018.03.03")
                .source(SearchSourceBuilder().size(200))
                .scroll(TimeValue(2, TimeUnit.MINUTES)))
        val dataArrays = mutableListOf<Array<Any>>()
        do {
            val searchHits = response.hits.hits
            if (response.hits.hits.size > 0) {
                searchHits.forEach { x ->
                    dataArrays.add(x.sourceAsMap.values.toTypedArray())
                }
            }
            if (dataArrays.size > 10000) {
                startIndex = startIndex + dataArrays.size
                for (j in 0.rangeTo(dataArrays.size - 1)) {
                    val row = sheet.createRow(startIndex + j)
                    val d = dataArrays[j]
                    for (k in 0.rangeTo(d.size - 1)) {
                        row.createCell(k).setCellValue(d[k].toString())
                    }
                }
                // 清楚数据，释放内存
                dataArrays.clear()

                println("startIndex ======================================== $startIndex ======= ${Date().datetime2string()}")
            }
            val hs = measureTimeMillis {
                response = client.searchScroll(SearchScrollRequest()
                        .scrollId(response.scrollId)
                        .scroll(TimeValue(2, TimeUnit.MINUTES)))
            }
//            println("读取200行，耗时：$hs ms")
        } while (response.hits.hits.size > 0)

        if (dataArrays.size > 0) {
            startIndex = startIndex + dataArrays.size
            for (j in 0.rangeTo(dataArrays.size - 1)) {
                val row = sheet.createRow(startIndex + j)
                val d = dataArrays[j]
                for (k in 0.rangeTo(d.size - 1)) {
                    row.createCell(k).setCellValue(d[k].toString())
                }
            }
            println("startIndex ======================================== $startIndex ======= ${Date().datetime2string()}")
            println("数据处理完成，dataArrays size: ${dataArrays.size}")
            // 清楚数据，释放内存
            dataArrays.clear()
        }
        xwb.write(f.outputStream())
        xwb.close()
    }
    println("els 处理完成，总耗时：${expose_time_} ms.")
    client.close()
}