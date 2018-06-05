package com.es.common

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
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

    fun genExcelAddContent(wb: Workbook, excelFile: File, headers: Array<String>, datas: List<Array<String>>, startIndex: Int = 0) {
        val sheet = wb.createSheet()
        for (j in startIndex.rangeTo(datas.size - 1)) {
            val row = sheet.createRow(j)
            for (k in 0.rangeTo(headers.size - 1)) {
                row.createCell(k).setCellValue(datas[j][k])
            }
        }
        wb.write(excelFile.outputStream())
    }

    fun genExcel(excelType: ExcelType, excelFile: File, headers: Array<String>, datas: List<Array<String>>, startIndex: Int = 0) {
        val wb = when (excelType) {
            ExcelType.XLS -> HSSFWorkbook()
            ExcelType.XLSX -> XSSFWorkbook()
            else -> XSSFWorkbook()
        }
        val sheet = wb.createSheet()
        for (j in startIndex.rangeTo(datas.size - 1)) {
            val row = sheet.createRow(j)
            for (k in 0.rangeTo(headers.size - 1)) {
                row.createCell(k).setCellValue(datas[j][k])
            }
        }
        wb.write(excelFile.outputStream())
        wb.close()
    }
}