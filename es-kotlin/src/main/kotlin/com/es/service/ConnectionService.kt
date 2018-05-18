package com.enlink.eslogs.service

import com.enlink.eslogs.common.StatusCodes
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit


class ConnectionNotAuthorized(message: String = StatusCodes.UNAUTHORIZED.message, status_code: Int = StatusCodes.UNAUTHORIZED.code)

class ConnectionService

fun ping(ip: String, port: String, scheme: String = "http"): Boolean {
    try {
        val client: OkHttpClient = OkHttpClient.Builder().readTimeout(3, TimeUnit.MINUTES).build()
        val response: Response = client.newCall(Request.Builder().url(scheme + "://" + ip + ":" + port).build()).execute()
        return response.isSuccessful
    } catch (e: Exception) {
        return false
    }
}

fun create_connection(ip: String, port: String, scheme: String = "http", username: String = "", password: String = "",
                      fail_on_exception: Boolean = false, enable_ssl: Boolean = false, ca_certs: String = "") {

}