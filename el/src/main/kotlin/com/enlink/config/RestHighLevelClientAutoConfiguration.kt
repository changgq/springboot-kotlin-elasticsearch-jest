package com.enlink.config

import com.enlink.config.properties.ElasticProps
import com.google.gson.Gson
import org.apache.coyote.http11.Constants.a
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
import org.elasticsearch.client.RestClient.builder
import org.elasticsearch.client.RestClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Springboot自动注入Elasticsearch的RestHighLevelClient客户端
 * @author changgq
 */
@Configuration
@ConditionalOnClass(RestHighLevelClient::class)
@AutoConfigureAfter(GsonAutoConfiguration::class)
open class RestHighLevelClientAutoConfiguration(
        var properties: ElasticProps,
        var gsonProvider: ObjectProvider<Gson>,
        var builderCustomizers: ObjectProvider<List<HttpClientConfigBuilderCustomizer>>?
) {
    val LOGGER : Logger = LoggerFactory.getLogger(RestHighLevelClientAutoConfiguration::class.java)

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    open fun highLevelClient(): RestHighLevelClient {
        return RestHighLevelClient(createClients())
    }

    private fun createClients(): RestClientBuilder {
        LOGGER.info("集群地址：${properties.uris.get(0)}")
        return RestClient.builder(*Array<HttpHost>(properties.uris.size, { x -> HttpHost.create(properties.uris.get(x)) }))
    }
}
