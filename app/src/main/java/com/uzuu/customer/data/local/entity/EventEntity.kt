package com.uzuu.customer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.uzuu.customer.data.local.Converters

@Entity(tableName = "events")
@TypeConverters(Converters::class)
data class EventEntity(
    @PrimaryKey
    val id: Long,
    val categoryId: Long = -1,
    val name: String,
    val categoryName: String,
    val location: String,
    val startTime: String?,
    val endTime: String?,
    val saleStartDate: String?,
    val saleEndDate: String?,
    val description: String?,
    val status: String,
    val imageUrls: String = "",
    val ticketTypes: String = "",
    val cachedAt: Long = System.currentTimeMillis()
)
