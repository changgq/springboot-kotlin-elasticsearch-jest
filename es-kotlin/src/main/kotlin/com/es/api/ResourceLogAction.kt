package com.es.api

import com.es.common.ApiResponse
import com.es.common.Condition
import com.es.common.RangeCondition
import com.es.common.SearchCondition
import com.es.services.ResourceLogService
import com.es.services.impl.ResourceLogServiceImpl
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController


/**
 * 资源日志相关Api
 * @author changgq
 */
@RestController
@RequestMapping("/resLog")
class ResourceLogAction(val highLevelClient: RestHighLevelClient) {
    val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)
    val resourceLogService: ResourceLogService = ResourceLogServiceImpl(highLevelClient)

    /**
     * @api {POST} api/log/resLog/resourceVisitRank 1_获取资源访问排行柱状图数据
     * @apiGroup resLog
     * @apiVersion 1.0.0
     * @apiDescription 获取资源访问排行柱状图数据
     * @apiParam {RangeCondition} rangeCondition 范围条件
     * @apiParam {int} topCount 指定返回结果包含前几名
     * @apiParamExample {String} 请求样例：
     * api/log/resLog/resourceVisitRank?topCount=5
     * @apiParamExample {json} 请求Body样例：
     * {
     * "type": "DATE",
     * "conditionName": "date",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": {
     * "xAxis_String": [
     * "Unique",
     * "CTrip",
     * "Tecent",
     * "Baidu",
     * "Youku"
     * ],
     * "xAxis_Integer": null,
     * "xAxis_Float": null,
     * "yAxis_String": null,
     * "yAxis_Integer": [
     * 88118,
     * 88094,
     * 88085,
     * 88056,
     * 88046
     * ],
     * "yAxis_Float": null
     * },
     * "extend": null
     * }
     */
    @RequestMapping(value = "/resourceVisitRank", method = arrayOf(RequestMethod.POST))
    fun resourceVisitRank(@RequestBody rangeCons: RangeCondition, topCount: Int = 10, sortBy: Boolean = false): ApiResponse {
        return resourceLogService.resourceVisitRank(rangeCons, topCount, sortBy)
    }

    //获取资源访问总次数
    @RequestMapping(value = "/resTotalCount", method = arrayOf(RequestMethod.POST))
    fun resTotalCount(@RequestBody cons: RangeCondition): ApiResponse {
        return resourceLogService.resTotalCount(cons)
    }

    //根据下载数量计算资源排名
    @RequestMapping(value = "/resourceOrderByType", method = arrayOf(RequestMethod.POST))
    fun resourceOrderByType(@RequestBody rangeCons: RangeCondition, topCount: Int = 10, sortBy: Boolean = false): ApiResponse {
        return resourceLogService.resourceOrderByType(rangeCons, topCount, sortBy)
    }

    //根据下载流量计算资源排名
    @RequestMapping(value = "/resourceOrderByDownload", method = arrayOf(RequestMethod.POST))
    fun resourceOrderByDownload(@RequestBody rangeCons: RangeCondition, topCount: Int = 10, sortBy: Boolean = false): ApiResponse {
        return resourceLogService.resourceOrderByDownload(rangeCons, topCount, sortBy)
    }

    /**
     * @api {POST} api/log/resLog/applicationPie 获取访问应用类型占比饼图数据
     * @apiGroup resLog
     * @apiVersion 0.0.1
     * @apiDescription 获取访问应用类型占比饼图数据
     * @apiParam {RangeCondition} rangeCondition 范围条件
     * @apiParamExample {String} 请求样例：
     * api/log/resLog/applicationPie
     * @apiParamExample {json} 请求Body样例：
     * {
     * "type": "DATE",
     * "conditionName": "date",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": {
     * "Windows7": 135740,
     * "Android4": 135648,
     * "iOS9": 135049,
     * "Windows10": 134935,
     * "Android5": 134659,
     * "iOS10": 134564,
     * "Windows8": 134518
     * },
     * "extend": null
     * }
     */
    @RequestMapping(value = "/applicationPie", method = arrayOf(RequestMethod.POST))
    fun applicationPie(@RequestBody rangeCondition: RangeCondition): ApiResponse {
        return resourceLogService.applicationPie(rangeCondition)
    }

    /**
     * @api {POST} api/log/resLog/linkTrafficRank 获取上传/下载资源排行柱状图数据
     * @apiGroup resLog
     * @apiVersion 0.0.1
     * @apiDescription 获取上传/下载资源排行柱状图数据
     * @apiParam {RangeCondition} rangeCondition 范围条件
     * @apiParam {int} topCount 指定返回结果包含前几名
     * @apiParam {String} type 类型,可取值up、down，分别对应上传和下载
     * @apiParamExample {String} 请求样例：
     * api/log/resLog/linkTrafficRank?topCount=5&type=down
     * @apiParamExample {json} 请求Body样例：
     * {
     * "type": "DATE",
     * "conditionName": "date",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": {
     * "xAxis_String": [
     * "Baidu",
     * "Tecent",
     * "BiliBili",
     * "CTrip",
     * "Unique",
     * "Alibaba",
     * "Amazon"
     * ],
     * "xAxis_Integer": null,
     * "xAxis_Float": null,
     * "yAxis_String": null,
     * "yAxis_Integer": [
     * 44175,
     * 44066,
     * 44064,
     * 44040,
     * 44034,
     * 44028,
     * 43997
     * ],
     * "yAxis_Float": null
     * },
     * "extend": null
     * }
     */
    @RequestMapping(value = "/linkTrafficRank", method = arrayOf(RequestMethod.POST))
    fun linkTrafficRank(@RequestBody rangeCondition: RangeCondition, topCount: Int = 10, type: String? = "total"): ApiResponse {
        return resourceLogService.linkTrafficRank(rangeCondition, topCount, type)
    }

    @RequestMapping(value = "/fileFormatPie", method = arrayOf(RequestMethod.POST))
    fun fileFormatPie(@RequestBody rangeCondition: RangeCondition, type: String? = "total"): ApiResponse {
        return resourceLogService.fileFormatPie(rangeCondition, type)
    }

    @RequestMapping(value = "/resourceVisitGroupStatics", method = arrayOf(RequestMethod.POST))
    fun resourceVisitGroupStatics(@RequestBody condition: Condition): ApiResponse {

//        for (index in 1..1) {
//            condition.currentPage = index
//            val apiResponse = resourceLogService.resourceVisitGroupStatics(condition)
//            val data = apiResponse.data
//            if (data is BasePage) {
//                condition.scrollId = data.scrollId
//            }
//        }

        return resourceLogService.resourceVisitGroupStatics(condition)
    }

    @RequestMapping(value = "/resourceVisitDetails", method = arrayOf(RequestMethod.POST))
    fun resourceVisitDetails(@RequestBody condition: SearchCondition): ApiResponse {
        return resourceLogService.resourceVisitDetails(condition)
    }
}