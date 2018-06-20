package com.es

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.GsonHttpMessageConverter


@SpringBootApplication
@EnableScheduling
open class Application {
    @Bean
    open fun gsonHttpMessageConverters(): HttpMessageConverters {
        return HttpMessageConverters(GsonHttpMessageConverter())
    }
}

fun main(args: Array<String>) {
    val app = SpringApplication(Application::class.java)
    app.run(*args)
    println("System is Running!")
}
