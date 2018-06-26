package com.enlink.model.dto

import com.enlink.model.BaseModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 功能描述：
 *
 * @auther changgq
 * @date 2018/6/26 15:15
 * @description
 */
open class BaseLogDto : BaseModel() {
    @Expose
    var type: String = ""
    @Expose
    @SerializedName("@version")
    var version: String = ""
    @Expose
    @SerializedName("@timestamp")
    var timestamp: String = ""
    @Expose
    var message: String = ""
}