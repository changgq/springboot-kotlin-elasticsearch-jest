package com.enlink.controller

import com.enlink.platform.CommonResponse
import com.enlink.platform.GsonUtils
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/cluster")
class ClusterController : BaseController() {

    /**
     * @api {GET} /api/cluster/info 1_集群基本信息
     * @apiGroup cluster
     * @apiVersion 1.0.0
     * @apiDescription 集群基本信息
     * @apiSuccess {ApiResponse} apiResponse Json格式返回响应结果
     * @apiSuccessExample {json} 返回样例:
     * {"data":[{"clusterName":"eslogs","clusterUuid":"03VfExIgRbGSp5tcW8jsxQ","nodeName":"master","version":"6.2.4","buildTime":"2018-04-12T20:37:28.497551Z"}],"response_time":71,"status_code":200,"message":"OK"}
     */
    @GetMapping("/info")
    fun info(): CommonResponse {
        var data = emptyMap<String, Any>()
        val elapsed_time_ = measureTimeMillis {
            val info = client.info()
            data = mapOf(
                    "clusterName" to info.clusterName.value(),
                    "clusterUuid" to info.clusterUuid,
                    "nodeName" to info.nodeName,
                    "version" to info.version.toString(),
                    "buildTime" to info.build.date()
            )
        }
        LOGGER.info(GsonUtils.convert(data))
        return CommonResponse(data, elapsed_time_)
    }
}