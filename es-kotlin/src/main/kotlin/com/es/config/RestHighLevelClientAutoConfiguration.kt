package com.es.config

import com.google.gson.Gson
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.elasticsearch.jest.HttpClientConfigBuilderCustomizer
import org.springframework.boot.autoconfigure.elasticsearch.jest.JestProperties
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder

/**
 * Springboot自动注入Elasticsearch的RestHighLevelClient客户端
 * @author changgq
 */
@Configuration
@ConditionalOnClass(RestHighLevelClient::class)
@EnableConfigurationProperties(JestProperties::class)
@AutoConfigureAfter(GsonAutoConfiguration::class)
open class RestHighLevelClientAutoConfiguration {

    private var properties: JestProperties

    private var gsonProvider: ObjectProvider<Gson>

    private var builderCustomizers: List<HttpClientConfigBuilderCustomizer>? = null

    constructor(properties: JestProperties, gson: ObjectProvider<Gson>,
                builderCustomizers: ObjectProvider<List<HttpClientConfigBuilderCustomizer>>) {
        this.properties = properties
        this.gsonProvider = gson
        this.builderCustomizers = builderCustomizers.ifAvailable
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    open fun highLevelClient(): RestHighLevelClient {
        return RestHighLevelClient(createRestClient())
    }

    private fun createRestClient(): RestClientBuilder {
        var defaultHost = HttpHost.create("http://127.0.0.1:9200")
        if (this.properties.uris.size > 0) {
            defaultHost = HttpHost.create(this.properties.uris.get(0))
        }
        val builder = RestClient.builder(defaultHost)
        return builder
    }
}
