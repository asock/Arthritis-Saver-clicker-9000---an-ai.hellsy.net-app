package com.assclk9000.app.data.db

import android.util.Base64
import androidx.room.TypeConverter
import com.assclk9000.app.data.model.ActionCondition
import com.assclk9000.app.data.model.ConditionType
import com.assclk9000.app.data.model.GestureType
import com.assclk9000.app.data.model.ScheduleConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class Converters {

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(ByteArray::class.java, ByteArrayAdapter())
            .create()
    }

    // GestureType <-> String
    @TypeConverter
    fun fromGestureType(value: GestureType): String {
        return value.name
    }

    @TypeConverter
    fun toGestureType(value: String): GestureType {
        return GestureType.valueOf(value)
    }

    // ActionCondition <-> JSON String
    @TypeConverter
    fun fromActionCondition(value: ActionCondition?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toActionCondition(value: String?): ActionCondition? {
        return value?.let { gson.fromJson(it, ActionCondition::class.java) }
    }

    // ScheduleConfig <-> JSON String
    @TypeConverter
    fun fromScheduleConfig(value: ScheduleConfig): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toScheduleConfig(value: String): ScheduleConfig {
        return gson.fromJson(value, ScheduleConfig::class.java)
    }

    // ByteArray <-> Base64 String
    @TypeConverter
    fun fromByteArray(value: ByteArray?): String? {
        return value?.let { Base64.encodeToString(it, Base64.DEFAULT) }
    }

    @TypeConverter
    fun toByteArray(value: String?): ByteArray? {
        return value?.let { Base64.decode(it, Base64.DEFAULT) }
    }

    /**
     * Custom Gson adapter to serialize/deserialize ByteArray as Base64 strings
     * within JSON objects (e.g., inside ActionCondition).
     */
    private class ByteArrayAdapter : JsonSerializer<ByteArray>, JsonDeserializer<ByteArray> {
        override fun serialize(
            src: ByteArray?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return if (src != null) {
                com.google.gson.JsonPrimitive(Base64.encodeToString(src, Base64.DEFAULT))
            } else {
                com.google.gson.JsonNull.INSTANCE
            }
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): ByteArray? {
            return if (json != null && !json.isJsonNull) {
                Base64.decode(json.asString, Base64.DEFAULT)
            } else {
                null
            }
        }
    }
}
