package com.tvlive.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room 类型转换器
 */
class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringMap(value: String?): Map<String, String>? {
        if (value == null) return null
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }
    
    @TypeConverter
    fun toStringMap(map: Map<String, String>?): String? {
        if (map == null) return null
        return gson.toJson(map)
    }
}
