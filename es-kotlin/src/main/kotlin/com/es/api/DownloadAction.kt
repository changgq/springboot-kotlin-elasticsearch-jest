package com.es.api

import com.es.common.ApiResponse
import com.es.common.GsonUtils
import com.es.common.SearchCondition
import com.es.common.ZipCompressor
import com.es.services.impl.DownloadServiceImpl
import com.fasterxml.jackson.core.type.TypeReference
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.*
import java.net.URLDecoder
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.ServletException
import javax.servlet.http.HttpServletResponse

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/4 15:22
 * @description
 */
@RestController
@RequestMapping("/download")
class DownloadAction(highLevelClient: RestHighLevelClient) : BaseAction(highLevelClient) {
    private val tmpFileDictionary = "/tmp/"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH-mm-ss")
    val downloadServie = DownloadServiceImpl(highLevelClient)

    /**
     * @api {GET} /api/log/download/zip 打包下载日志
     * @apiGroup Download
     * @apiVersion 0.0.1
     * @apiDescription 查询符合匹配条件的日志，然后压缩成zip包下载<span style="color:red">【没有考虑exlx单表单sheet超过最大行数的情况！】</span>
     * @apiParam {String} match 匹配字段，是List<DownloadCondition>的json字符串经过js函数encodeURLComponent转码后的字符串
     * @apiParamExample {String} 请求样例：
     * /api/log/download/zip?match=%5B%7B%22preciseConditions%22%3A%7B%22logLevel.keyword%22%3A%5B%22WARNING%22%2C%22INFO%22%5D%7D%2C%22ambiguousConditions%22%3A%7B%7D%2C%22rangeConditionList%22%3A%5B%7B%22type%22%3A%22DATE%22%2C%22conditionName%22%3A%22date%22%2C%22gteValue%22%3A%222017-06-13%22%2C%22lteValue%22%3A%222017-08-16%22%7D%5D%2C%22logType%22%3A%22adminLog%22%7D%2C%7B%22preciseConditions%22%3A%7B%22logLevel.keyword%22%3A%5B%22WARNING%22%2C%22INFO%22%5D%7D%2C%22ambiguousConditions%22%3A%7B%7D%2C%22rangeConditionList%22%3A%5B%7B%22type%22%3A%22DATE%22%2C%22conditionName%22%3A%22date%22%2C%22gteValue%22%3A%222017-06-13%22%2C%22lteValue%22%3A%222017-08-16%22%7D%5D%2C%22logType%22%3A%22userLog%22%7D%5D
     * @apiSuccess (200) {Response} response 返回信息封装类<span style="color:red">【下载成功压缩包命名为随机生成字符串】</span>
     * @apiSuccessExample {json} 返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": null,
     * "extend": null
     * }
    </DownloadCondition> */
    @RequestMapping(value = "/zip", method = arrayOf(RequestMethod.GET))
    fun downloadAll(@RequestParam("match") match: String, response: HttpServletResponse): ApiResponse {
        LOGGER.info(" ------------->> $match")

        val conditions = GsonUtils.reConvert(URLDecoder.decode(match, "utf-8"), SearchCondition::class.java)
        LOGGER.info(conditions.toString())

        // 获取Excel文件
        val filePaths: Array<String> = downloadServie.download(conditions)
        val zipName = dateFormat.format(Date()) + ".zip"
        val zipPath = tmpFileDictionary + "日志下载内容" + "_" + zipName
        LOGGER.trace("generate zipPath[$zipPath]")

        // 生成Zip文件
        ZipCompressor(zipPath).compress(*filePaths)

        // 文件下载
        val zipf = File(zipPath)
        val fis = BufferedInputStream(zipf.inputStream())
        val buffer = ByteArray(fis.available())
        fis.read(buffer)
        fis.close()
        val bufferOut = BufferedOutputStream(response.outputStream)
        response.contentType = "application/octet-stream"
        response.setHeader("Content-Disposition", "attachment;filename=" + zipf.name.toByteArray(Charset.defaultCharset()))
        bufferOut.write(buffer)
        bufferOut.flush()
        bufferOut.close()

        return ApiResponse("")
    }
}