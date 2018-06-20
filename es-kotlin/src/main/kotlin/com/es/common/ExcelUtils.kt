package com.es.common

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

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