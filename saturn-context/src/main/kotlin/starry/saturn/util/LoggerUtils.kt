package starry.saturn.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public fun Any.getLogger(): Logger = LoggerFactory.getLogger(this::class.java)
