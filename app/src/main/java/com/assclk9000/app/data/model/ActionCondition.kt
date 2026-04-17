package com.assclk9000.app.data.model

enum class ConditionType {
    NONE,
    WAIT_FOR_IMAGE,
    CLICK_ON_IMAGE,
    STOP_IF_IMAGE_GONE
}

data class ActionCondition(
    val type: ConditionType = ConditionType.NONE,
    val imageData: ByteArray? = null,
    val regionX: Int = 0,
    val regionY: Int = 0,
    val regionWidth: Int = 0,
    val regionHeight: Int = 0,
    val similarityThreshold: Float = 0.9f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActionCondition

        if (type != other.type) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        if (regionX != other.regionX) return false
        if (regionY != other.regionY) return false
        if (regionWidth != other.regionWidth) return false
        if (regionHeight != other.regionHeight) return false
        if (similarityThreshold != other.similarityThreshold) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        result = 31 * result + regionX
        result = 31 * result + regionY
        result = 31 * result + regionWidth
        result = 31 * result + regionHeight
        result = 31 * result + similarityThreshold.hashCode()
        return result
    }
}
