package com.uzuu.customer.domain.model

data class Order(
    val id: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val orderStatus: String,
    val orderDate: String,
    val paymentUrl: String? = null
)