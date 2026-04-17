package com.assclk9000.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "click_profiles")
data class ClickProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    val description: String = "",

    val createdAt: Long,

    val updatedAt: Long,

    val scheduleConfig: ScheduleConfig = ScheduleConfig(),

    val globalRepeatCount: Int = 1,

    val globalIntervalMs: Long = 0L,

    val isEnabled: Boolean = true
)
