package com.es

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.text.SimpleDateFormat
import java.util.*

@SpringBootApplication
@EnableScheduling
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
    println("System is Running!")
}

inline fun Date.date2string_point(): String {
    return SimpleDateFormat("yyyy.MM.dd").format(this)
}

inline fun Date.date2string(): String {
    return SimpleDateFormat("yyyy-MM-dd").format(this)
}

inline fun Date.datetime2string(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this)
}

inline fun String.string2date(): Date {
    return SimpleDateFormat("yyyy-MM-dd").parse(this)
}

inline fun String.string2datetime(): Date {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(this)
}

inline fun String.checkIsDate(): Boolean {
    try {
        return this.string2date().date2string() == this
    } catch (e: Exception) {
        return false
    }
}