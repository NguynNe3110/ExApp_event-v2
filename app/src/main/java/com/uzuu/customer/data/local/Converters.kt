package com.uzuu.customer.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromCategoryTicketList(value: List<Map<String, Any>>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCategoryTicketList(value: String): List<Map<String, Any>> {
        val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
        return gson.fromJson(value, listType)
    }
}
