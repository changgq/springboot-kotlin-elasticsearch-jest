package com.es.api

import com.es.common.ApiResponse
import com.es.common.RangeCondition
import com.es.common.SearchCondition
import com.es.services.ResourceLogService
import com.es.services.impl.ResourceLogServiceImpl
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * 功能描述： 流量Action
 *
 * @auther changgq
 * @date 2018/5/31 20:59
 * @description
 */
@RestController
@RequestMapping("/traffic")
class TrafficAction(highLevelClient: RestHighLevelClient) : BaseAction(highLevelClient) {
    val resourceLogService: ResourceLogService = ResourceLogServiceImpl(highLevelClient)

    /**
     * @api {POST} api/log/traffic/trafficRank 获取用户/应用资源流量排行柱状图数据
     * @apiGroup traffic
     * @apiVersion 0.0.1
     * @apiDescription 获取用户/应用资源流量排行柱状图数据
     * @apiParam {String} type 类型，可取值user、resource，分别对应用户流量和应用资源流量
     * @apiParam {RangeCondition} rangeCondition 范围条件
     * @apiParam {int} topCount 指定返回结果包含前几名
     * @apiParamExample {String} 请求样例：
     * api/log/traffic/trafficRank?topCount=5&type=user
     * @apiParamExample {json} 请求Body样例：
     * {
     * "type": "DATE",
     * "conditionName": "date",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 应用资源流量返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": {
     * "Apple": 65162630,
     * "iQiyi": 65553404,
     * "Alibaba": 65684128,
     * "Uber": 65754467,
     * "Amazon": 65765013,
     * "CTrip": 65856752,
     * "Tecent": 65868054
     * },
     * "extend": null
     * }
     * @apiSuccessExample {json} 用户流量返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": {
     * "LiJianYe/JianYeGroup": 1552835,
     * "WeiJianGuo/FangGroup": 1554586,
     * "ZhengZhengQuan/MingGroup": 1556441,
     * "WangZhengQuan/JianYeGroup": 1560144,
     * "ZhengLan/JianYeGroup": 1562874,
     * "WuJianYe/ZhengQuanGroup": 1567552,
     * "QianFang/LanGroup": 1569990
     * },
     * "extend": null
     * }
     */
    @RequestMapping(value = "/trafficRank", method = arrayOf(RequestMethod.POST))
    fun getTrafficRank(@RequestBody rangeCondition: RangeCondition, topCount: Int, type: String): ApiResponse {
        return resourceLogService.getTrafficRank(rangeCondition, topCount, type)
    }

    //根据用户登录使用流量排名，获取用户登录次数
    @RequestMapping(value = "/userFlowAndLoginCount", method = arrayOf(RequestMethod.POST))
    fun getUserFlowAndLoginCount(@RequestBody rangeCondition: RangeCondition, topCount: Int): ApiResponse {
        return resourceLogService.getUserFlowAndLoginCount(rangeCondition, topCount)
    }


    /**
     * @api {POST} api/log/traffic/trafficPie 获取用户组/应用资源流量占比饼图数据
     * @apiGroup traffic
     * @apiVersion 0.0.1
     * @apiDescription 获取用户组/应用资源流量占比饼图数据
     * @apiParam {String} type 类型，可取值user、resource，分别对应用户组流量和应用资源流量
     * @apiParam {RangeCondition} rangeCondition 范围条件
     * @apiParamExample {String} 请求样例：
     * api/log/traffic/trafficPie?type=user
     * @apiParamExample {json} 请求Body样例：
     * {
     * "type": "DATE",
     * "conditionName": "date",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 应用资源流量返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": {
     * "192.168.5.11": "2950567.0",
     * "192.168.5.88": "2950278.0",
     * "192.168.5.249": "2977175.0",
     * "192.168.5.144": "2952711.0",
     * "192.168.5.228": "2930616.0",
     * "192.168.5.51": "2936684.0",
     * "192.168.5.229": "2925252.0",
     * "192.168.5.121": "2902576.0",
     * "192.168.5.190": "2965689.0",
     * "192.168.5.129": "2913525.0"
     * },
     * "extend": null
     * }
     * @apiSuccessExample {json} 用户组流量返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": {
     * "FangGroup": "1.03526427E8",
     * "ZhengQuanGroup": "1.03576141E8",
     * "JianYeGroup": "1.03423239E8",
     * "MingGroup": "1.03602251E8",
     * "GangGroup": "1.03280324E8",
     * "JianGuoGroup": "1.02938135E8",
     * "LanGroup": "1.03226898E8"
     * },
     * "extend": null
     * }
     */
    @RequestMapping(value = "/trafficPie", method = arrayOf(RequestMethod.POST))
    fun getTrafficPie(@RequestBody rangeCondition: RangeCondition, type: String): ApiResponse {
        return resourceLogService.getTrafficPie(rangeCondition, type)
    }


    /**
     * @api {POST} api/log/traffic/trafficGroupStatics 获取用户/资源流量统计表格数据
     * @apiGroup traffic
     * @apiVersion 0.0.1
     * @apiDescription 获取用户/资源流量统计表格数据
     * @apiParam {String} type 类型，可取值user、resource，分别对应用户流量和资源流量
     * @apiParam {Condition} condition 范围条件
     * @apiParamExample {String} 请求样例：
     * api/log/traffic/trafficGroupStatics?type=user
     * @apiParamExample {json} 请求Body样例：
     * {
     * "preciseConditions": {
     *
     *
     * },
     * "ambiguousConditions": {
     *
     *
     * },
     * "rangeConditionList": [
     * {
     * "type": "DATE",
     * "conditionName": "date",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * ],
     * "sortConditions": {
     *
     *
     * },
     * "currentPage": 1,
     * "pageSize": 5
     * }
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 用户流量统计返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": [
     * {
     * "totalUploadTraffic": "1047337.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "512807.0",
     * "userName": "WangZhengQuan",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "userGroup": "JianYeGroup",
     * "totalTraffic": "1560144.0"
     * },
     * {
     * "totalUploadTraffic": "1020749.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "510982.0",
     * "userName": "WangZhengQuan",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "userGroup": "GangGroup",
     * "totalTraffic": "1531731.0"
     * },
     * {
     * "totalUploadTraffic": "1031575.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "501995.0",
     * "userName": "WangZhengQuan",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "userGroup": "MingGroup",
     * "totalTraffic": "1533570.0"
     * },
     * {
     * "totalUploadTraffic": "999199.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "507741.0",
     * "userName": "WangZhengQuan",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "userGroup": "ZhengQuanGroup",
     * "totalTraffic": "1506940.0"
     * },
     * {
     * "totalUploadTraffic": "991639.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "497053.0",
     * "userName": "WangZhengQuan",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "userGroup": "FangGroup",
     * "totalTraffic": "1488692.0"
     * }
     * ],
     * "extend": {
     * "page": {
     * "currentPage": 5,
     * "pageSize": 1,
     * "totalHits": 490,
     * "pageCount": 490
     * }
     * }
     * }
     * @apiSuccessExample {json} 资源流量统计返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": [
     * {
     * "totalUploadTraffic": "513737.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "263419.0",
     * "resourceName": "Unique",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "uri": "DEL/interface/9",
     * "totalTraffic": "777156.0"
     * },
     * {
     * "totalUploadTraffic": "522069.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "256678.0",
     * "resourceName": "Unique",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "uri": "UPDATE/name/2",
     * "totalTraffic": "778747.0"
     * },
     * {
     * "totalUploadTraffic": "518333.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "250909.0",
     * "resourceName": "Unique",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "uri": "UPDATE/pswd/5",
     * "totalTraffic": "769242.0"
     * },
     * {
     * "totalUploadTraffic": "513187.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "252441.0",
     * "resourceName": "Unique",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "uri": "ADD/pswd/8",
     * "totalTraffic": "765628.0"
     * },
     * {
     * "totalUploadTraffic": "518078.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "249838.0",
     * "resourceName": "Unique",
     * "lastVisitDate": "2017-06-30 16:23:52",
     * "uri": "UPDATE/pswd/2",
     * "totalTraffic": "767916.0"
     * }
     * ],
     * "extend": {
     * "page": {
     * "currentPage": 5,
     * "pageSize": 1,
     * "totalHits": 990,
     * "pageCount": 990
     * }
     * }
     * }
     */
    @RequestMapping(value = "/trafficGroupStatics", method = arrayOf(RequestMethod.POST))
    fun getTrafficGroupStatics(@RequestBody condition: SearchCondition, type: String): ApiResponse {
        return resourceLogService.getTrafficGroupStatics(condition, type)
    }


    /**
     * @api {POST} api/log/traffic/trafficDetails 获取流量表格【明细】数据
     * @apiGroup traffic
     * @apiVersion 0.0.1
     * @apiDescription 获取流量表格【明细】数据
     * @apiParam {String} type 类型，可取值user、resource
     * @apiParam {Condition} condition 范围条件
     * @apiParamExample {String} 请求样例：
     * api/log/traffic/trafficDetails?type=user
     * @apiParamExample {json} 应用资源流量请求Body样例：
     * {
     * "preciseConditions": {
     * "resourceName.keyword":["Unique"],
     * "uri.keyword":["DEL/interface/9"]
     * },
     * "ambiguousConditions": {
     *
     *
     * },
     * "rangeConditionList": [
     * {
     * "type": "DATE",
     * "conditionName": "date",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * ],
     * "sortConditions": {
     *
     *
     * },
     * "currentPage": 1,
     * "pageSize": 5
     * }
     * @apiParamExample {json} 用户流量请求Body样例：
     * {
     * "preciseConditions": {
     * "userName.keyword": ["WangZhengQuan"],
     * "userGroup.keyword": ["LanGroup"]
     * },
     * "ambiguousConditions": {
     *
     *
     * },
     * "rangeConditionList": [
     * {
     * "type": "DATE",
     * "conditionName": "date",
     * "gteValue": "2017-06-01",
     * "lteValue": "2017-07-25",
     * "timeZone": "+08:00"
     * }
     * ],
     * "sortConditions": {
     *
     *
     * },
     * "currentPage": 1,
     * "pageSize": 5
     * }
     * @apiSuccess (200) {Response} response 返回信息封装类
     * @apiSuccessExample {json} 用户流量返回样例:
     *
     *
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": [
     * {
     * "totalUploadTraffic": "3223.0",
     * "firstVisitDate": "2017-06-02 16:23:52",
     * "totalDownloadTraffic": "2019.0",
     * "userName": "SunMing",
     * "lastVisitDate": "2017-06-27 16:23:52",
     * "userGroup": "JianYeGroup",
     * "totalTraffic": "5242.0"
     * },
     * {
     * "totalUploadTraffic": "4580.0",
     * "firstVisitDate": "2017-06-03 16:23:52",
     * "totalDownloadTraffic": "2245.0",
     * "userName": "SunMing",
     * "lastVisitDate": "2017-06-24 16:23:52",
     * "userGroup": "FangGroup",
     * "totalTraffic": "6825.0"
     * },
     * {
     * "totalUploadTraffic": "2513.0",
     * "firstVisitDate": "2017-06-03 16:23:52",
     * "totalDownloadTraffic": "1339.0",
     * "userName": "SunMing",
     * "lastVisitDate": "2017-06-29 16:23:52",
     * "userGroup": "JianGuoGroup",
     * "totalTraffic": "3852.0"
     * },
     * {
     * "totalUploadTraffic": "1107.0",
     * "firstVisitDate": "2017-06-03 16:23:52",
     * "totalDownloadTraffic": "682.0",
     * "userName": "SunMing",
     * "lastVisitDate": "2017-06-14 16:23:52",
     * "userGroup": "GangGroup",
     * "totalTraffic": "1789.0"
     * },
     * {
     * "totalUploadTraffic": "1635.0",
     * "firstVisitDate": "2017-06-16 16:23:52",
     * "totalDownloadTraffic": "717.0",
     * "userName": "SunMing",
     * "lastVisitDate": "2017-06-22 16:23:52",
     * "userGroup": "LanGroup",
     * "totalTraffic": "2352.0"
     * }
     * ],
     * "extend": {
     * "page": {
     * "currentPage": 5,
     * "pageSize": 1,
     * "totalHits": 431,
     * "pageCount": 431
     * }
     * }
     * }
     * @apiSuccessExample {json} 应用资源流量返回样例:
     * {
     * "status": "SUCCESS",
     * "code": null,
     * "data": [
     * {
     * "totalUploadTraffic": "3600.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "1022.0",
     * "resourceName": "BiliBili",
     * "lastVisitDate": "2017-06-28 16:23:52",
     * "uri": "DEL/pswd/0",
     * "totalTraffic": "4622.0"
     * },
     * {
     * "totalUploadTraffic": "2149.0",
     * "firstVisitDate": "2017-06-09 16:23:52",
     * "totalDownloadTraffic": "1404.0",
     * "resourceName": "BiliBili",
     * "lastVisitDate": "2017-06-25 16:23:52",
     * "uri": "ADD/interface/0",
     * "totalTraffic": "3553.0"
     * },
     * {
     * "totalUploadTraffic": "1825.0",
     * "firstVisitDate": "2017-06-07 16:23:52",
     * "totalDownloadTraffic": "1073.0",
     * "resourceName": "BiliBili",
     * "lastVisitDate": "2017-06-24 16:23:52",
     * "uri": "DEL/interface/6",
     * "totalTraffic": "2898.0"
     * },
     * {
     * "totalUploadTraffic": "3409.0",
     * "firstVisitDate": "2017-06-03 16:23:52",
     * "totalDownloadTraffic": "1508.0",
     * "resourceName": "BiliBili",
     * "lastVisitDate": "2017-06-27 16:23:52",
     * "uri": "DEL/name/0",
     * "totalTraffic": "4917.0"
     * },
     * {
     * "totalUploadTraffic": "2372.0",
     * "firstVisitDate": "2017-06-01 16:23:52",
     * "totalDownloadTraffic": "1120.0",
     * "resourceName": "BiliBili",
     * "lastVisitDate": "2017-06-29 16:23:52",
     * "uri": "UPDATE/interface/8",
     * "totalTraffic": "3492.0"
     * }
     * ],
     * "extend": {
     * "page": {
     * "currentPage": 5,
     * "pageSize": 1,
     * "totalHits": 856,
     * "pageCount": 856
     * }
     * }
     * }
     */
    @RequestMapping(value = "/trafficDetails", method = arrayOf(RequestMethod.POST))
    fun getTrafficDetails(@RequestBody condition: SearchCondition, type: String): ApiResponse {
        val t = when (type) {
            "user" -> "resource"
            "resource" -> "user"
            else -> "resource"
        }
        return resourceLogService.getTrafficDetails(condition, t)
    }
}