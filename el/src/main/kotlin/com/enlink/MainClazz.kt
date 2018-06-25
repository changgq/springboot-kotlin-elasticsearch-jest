package com.enlink

import com.enlink.config.properties.ElasticProps
import com.enlink.config.properties.PathProps
import com.enlink.platform.GsonUtils
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling
import java.text.SimpleDateFormat
import java.util.*


@EnableScheduling
@EnableConfigurationProperties(ElasticProps::class, PathProps::class)
@SpringBootApplication
open class MainClazz

fun main(args: Array<String>) {
    SpringApplication(MainClazz::class.java).run(*args)
}