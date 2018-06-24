package com.enlink.config.okhttp3

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 功能描述：OKHttp3连接客户端Spring自动注入
 *
 * @auther changgq
 * @date 2018/6/22 22:29
 * @description
 */
@Configuration
open class OkHttp3AutoConfiguration {
    @Bean
    open fun okHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.MINUTES)
                .writeTimeout(30, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true);
        return builder.build();
    }
}