package com.enlink

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.text.SimpleDateFormat
import java.util.*

@SpringBootApplication
@EnableScheduling
open class MainClazz

fun main(args: Array<String>) {
    SpringApplication(MainClazz::class.java).run(*args)
}