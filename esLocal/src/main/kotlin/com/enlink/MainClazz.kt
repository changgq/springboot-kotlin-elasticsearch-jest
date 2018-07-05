package com.enlink

import com.enlink.config.SystemLogProperties
import com.enlink.config.SystemProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableConfigurationProperties(SystemProperties::class, SystemLogProperties::class)
@SpringBootApplication
open class MainClazz

fun main(args: Array<String>) {
    SpringApplication(MainClazz::class.java).run(*args)
}