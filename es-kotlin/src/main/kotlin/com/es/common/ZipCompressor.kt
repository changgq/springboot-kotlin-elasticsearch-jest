package com.es.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.zip.CRC32
import java.util.zip.CheckedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.system.measureTimeMillis

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/4 16:54
 * @description
 */
class ZipCompressor(val zipPathName: String) {
    private val LOGGER: Logger = LoggerFactory.getLogger(ZipCompressor::class.java)
    private val BUFFER_SIZE = 8192
    private val zipFile: File

    init {
        zipFile = File(zipPathName)
    }

    fun compress(vararg filePaths: String) {
        val zipOut = ZipOutputStream(CheckedOutputStream(zipFile.outputStream(), CRC32()))
        filePaths.forEach { it ->
            val f = File(it)
            LOGGER.info("Path==>" + f.path)
            compressFile(f, zipOut, "")
        }
    }

    fun compressFile(f: File, zipOut: ZipOutputStream, baseDir: String) {
        if (f.exists()) {
            zipOut.putNextEntry(ZipEntry(baseDir + f.name))
            val buff = BufferedInputStream(f.inputStream())
            val data = ByteArray(BUFFER_SIZE)
            var isWrite = true
            while (isWrite) {
                val count = buff.read(data, 0, BUFFER_SIZE)
                if (count > 0) {
                    zipOut.write(data, 0, count)
                    continue
                }
                isWrite = false
            }
            buff.close()
            zipOut.close()
        }
    }
}

//fun main(args: Array<String>) {
//    ZipCompressor("D://测试/resource.zip").compress("D:\\EBook\\ES6标准入门（第二版）.pdf")
//    ZipCompressor("D://测试/resource2.zip").compress2("D:\\EBook\\ES6标准入门（第二版）.pdf")
//}