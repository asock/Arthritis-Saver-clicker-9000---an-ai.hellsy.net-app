package com.assclk9000.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "click_actions",
    foreignKeys = [
        ForeignKey(
            entity = ClickProfile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class ClickAction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "profileId")
    val profileId: Long,

    val orderIndex: Int,

    val gestureType: GestureType,

    val x: Float,
    val y: Float,

    val x2: Float? = null,
    val y2: Float? = null,

    val duration: Long = 50L,

    val intervalAfter: Long = 1000L,

    val jitterMs: Long = 0L,

    val jitterPx: Float = 0f,

    val repeatCount: Int = 1,

    val condition: ActionCondition? = null,

    val description: String = ""
)
