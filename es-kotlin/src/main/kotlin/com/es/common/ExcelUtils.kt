package com.es.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/5 18:43
 * @description
 */
object ExcelUtils {

    fun genExcel(headers: Array<String>, datas: List<Array<String>>, startIndex: Int = 0) {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet()
        for (j in datas.size.downTo(1)) {
            val row = sheet.createRow(j)
            for (k in headers.size.until(0)) {
                row.createCell(k).setCellValue(datas[j][k])
            }
        }
        workbook.write(File("d:\\a.xlsx"))
        workbook.close()
    }
}

fun main(args: Array<String>) {
    val headers = Array<String>(26) { i -> ('a' + i).toString() }
    val datas = List<Array<String>>(20, { y ->
        Array<String>(headers.size, { x -> ("X" + y + x) })
    })
    println(GsonBuilder().setPrettyPrinting().create().toJson(datas))
    ExcelUtils.genExcel(headers, datas, 0)
}