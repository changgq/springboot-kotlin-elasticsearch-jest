package com.enlink.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "system.path")
open class SystemProperties(var root: String = "", var admin: String = "", var user: String = "", var system: String = "", var res: String = "")


