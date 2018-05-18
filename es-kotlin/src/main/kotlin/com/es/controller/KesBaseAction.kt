package com.enlink.eslogs.controller

import io.searchbox.client.JestClient
import io.searchbox.client.JestResult
import io.searchbox.cluster.*
import io.searchbox.strings.StringUtils
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = "/api")
open class KesBaseAction(val jestClient: JestClient) {

    /**
     * 集群健康状况
     * @desc /_cluster/health
     * @return
     */
    @GetMapping(value = "/health")
    fun health() = jestClient.execute<JestResult>(Health.Builder().build()).jsonString

    /**
     * 集群状态
     * @desc /_cluster/state
     * @return
     */
    @GetMapping("/state")
    fun state() = jestClient.execute(State.Builder().build()).jsonString

    /**
     * 节点状态
     * @desc /_cluster/stats/nodes/ + nodes
     * @param nodes
     * @return
     */
    @GetMapping("/statsNodes/{nodes}")
    fun statsNodes(@PathVariable nodes: String) = if (!StringUtils.isBlank(nodes)) jestClient.execute(Stats.Builder().addNode(nodes).build()).jsonString else null

    /**
     * 执行中的任务
     * @desc /_cluster/pending_tasks
     * @return
     */
    @GetMapping("/pendingClusterTasks")
    fun pendingClusterTasks() = jestClient.execute(PendingClusterTasks.Builder().build()).jsonString

    /**
     * 集群设置
     * @desc /_cluster/settings
     * @return
     */
    @GetMapping("/settings")
    fun settings() = jestClient.execute(GetSettings.Builder().build()).jsonString

    /**
     * 更新集群设置
     * @desc /_cluster/settings PUT sources
     * @return
     */
    @PutMapping("/putSettings")
    fun putSettings(sources: String) = jestClient.execute(UpdateSettings.Builder(sources).build()).jsonString

    @GetMapping("/nodesInfo")
    fun nodesInfo() = jestClient.execute(NodesInfo.Builder().build()).jsonString

    @GetMapping("/nodesStats")
    fun nodesStats() = jestClient.execute(NodesStats.Builder().build()).jsonString
}
