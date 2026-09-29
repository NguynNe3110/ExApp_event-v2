package com.uzuu.customer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carts")
data class CartEntity(
    @PrimaryKey
    val id: Long,
    val totalAmount: Double,
    val updatedAt: Long = System.currentTimeMillis()
)
