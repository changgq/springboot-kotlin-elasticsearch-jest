package com.es.api

import com.es.common.ApiResponse
import io.searchbox.strings.StringUtils
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/index")
class Indices(val highLevelClient: RestHighLevelClient) {

    @RequestMapping(value = "query", method = arrayOf(RequestMethod.PUT, RequestMethod.POST))
    fun query(index: String, type: String, id: String?): ApiResponse {
        var url = ""
        if (!StringUtils.isBlank(index)) {
            url += "/" + index
        }
        if (!StringUtils.isBlank(type)) {
            url += "/" + type
        }
        if (!StringUtils.isBlank(id)) {
            url += "/" + id
        }
        println("接口调用成功！url：" + url)
        return ApiResponse(null, 0)
    }
}