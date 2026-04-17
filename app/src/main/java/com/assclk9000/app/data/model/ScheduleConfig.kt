package com.assclk9000.app.data.model

data class ScheduleConfig(
    val delayBeforeStart: Long = 0L,
    val stopAfterRepeats: Int = 0,
    val stopAfterMs: Long = 0L,
    val scheduledStartTime: Long? = null
)
