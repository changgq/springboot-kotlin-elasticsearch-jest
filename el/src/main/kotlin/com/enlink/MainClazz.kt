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
    println("System is Running!")

    val dateString = "Thu Sep 07 2017 00:00:00 +0800"
    val sdf = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss Z", Locale.ENGLISH)
    val dd = sdf.parse(dateString) //将字符串改为date的格式
    val resDate = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dd)
    println(resDate)
}