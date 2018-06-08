package com.es.api

import com.es.common.ApiResponse
import com.es.common.GsonUtils
import com.es.model.LogSetting
import com.es.services.LogSettingService
import com.es.services.impl.LogSettingServiceImpl
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.charset.Charset

/**
 * 功能描述：日志设置API
 *
 * @auther changgq
 * @date 2018/6/1 17:38
 * @description
 */
@RestController
@RequestMapping("/logConfig")
class LogConfigAction(highLevelClient: RestHighLevelClient) : BaseAction(highLevelClient) {
    private val logConfigPath = "d://usr/local/enlink/logJson.config"
    private var logSettingService: LogSettingService = LogSettingServiceImpl(highLevelClient)

    /**
     * 功能描述: 更新日志设置信息
     * @auther changgq
     * @date 2018/6/8 10:26
     *
     * @param
     * @return
     */
    @RequestMapping("/setting", method = arrayOf(RequestMethod.POST))
    fun updateSetting(@RequestBody logSetting: LogSetting): ApiResponse {
        val rel = logSettingService.update(logSetting)
        // 保存日志文件到服务器
        val f = File(logConfigPath)
        if (!f.exists()) {
            f.parentFile.mkdirs()
        }
        f.writeText(GsonUtils.convert(logSetting)!!, Charset.defaultCharset())
        return ApiResponse(rel)
    }

    /**
     * 功能描述: 获取日志设置信息
     * @auther changgq
     * @date 2018/6/8 10:25
     *
     * @param
     * @return
     */
    @GetMapping("/get")
    fun get(): ApiResponse {
        val ls = logSettingService.getById("1")
        return ApiResponse(ls)
    }
}