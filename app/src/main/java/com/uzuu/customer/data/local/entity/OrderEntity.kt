package com.uzuu.customer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val orderStatus: String,
    val orderDate: String,
    val paymentUrl: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
