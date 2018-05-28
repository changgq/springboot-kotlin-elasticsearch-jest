package com.es.api

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/nodes")
class Nodes(val highLevelClient: RestHighLevelClient) {

}