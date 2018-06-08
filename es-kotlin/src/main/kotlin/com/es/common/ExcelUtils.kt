package com.es.common

import com.es.datetime2string
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/5 18:43
 * @description
 */
object ExcelUtils {

    enum class ExcelType {
        XLS, XLSX
    }

    fun genExcel(excelType: ExcelType, filePath: String, headers: Array<String>, datas: List<Array<String>>, startIndex: Int = 0, sheetName: String = "sheet1") {
        val f = File(filePath)
        val wb = when (excelType) {
            ExcelType.XLS -> if (f.exists()) HSSFWorkbook(f.inputStream()) else HSSFWorkbook()
            else -> if (f.exists()) XSSFWorkbook(f.inputStream()) else XSSFWorkbook()
        }
        var sheet = wb.getSheet(sheetName)
        if (null == sheet) sheet = wb.createSheet(sheetName)
        for (j in 0.rangeTo(datas.size - 1)) {
            val row = sheet.createRow(startIndex + j)
            for (k in 0.rangeTo(headers.size - 1)) {
                row.createCell(k).setCellValue(datas[j][k])
            }
        }
        wb.write(f.outputStream())
        wb.close()
    }
}

fun main(args: Array<String>) {
//    val headers = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N")
//    println(GsonUtils.convert(headers))
//
//
//    var fileNameIndex = 0
//    for (a in 1..2000) {
//        val datas: List<Array<String>> = List(200, { x ->
//            Array(headers.size, { y -> headers[y] + x })
//        })
//        if (a % 50 == 0) {
//            fileNameIndex++
//        }
//        println("fileNameIndex: $fileNameIndex, startIndex: ${a % 50}")
//        ExcelUtils.genExcel(ExcelUtils.ExcelType.XLSX, "d://abc$fileNameIndex.xlsx", headers, datas, (a % 50) * 200)
//    }

    println(Date(2018-1900,5,8, 0,0,0).time)
    println(Date(1528387200000).datetime2string())
    println("1h".last().equals('s'))
}