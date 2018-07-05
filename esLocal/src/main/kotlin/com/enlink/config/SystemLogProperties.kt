package com.enlink.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "system.log")
open class SystemLogProperties(var admin: String = "", var user: String = "", var system: String = "", var res: String = "")