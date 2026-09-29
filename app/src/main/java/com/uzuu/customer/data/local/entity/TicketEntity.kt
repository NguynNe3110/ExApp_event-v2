package com.uzuu.customer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey
    val id: Long,
    val eventId: Long,
    val eventname: String,
    val ticketTypeId: Long,
    val ticketTypeName: String,
    val ticketCode: String,
    val qrCode: String,
    val status: String,
    val usedAt: String?,
    val cachedAt: Long = System.currentTimeMillis()
)