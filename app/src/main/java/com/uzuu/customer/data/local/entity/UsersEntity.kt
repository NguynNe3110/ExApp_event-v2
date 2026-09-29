package com.uzuu.customer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UsersEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val address: String,
    val url: String?
)