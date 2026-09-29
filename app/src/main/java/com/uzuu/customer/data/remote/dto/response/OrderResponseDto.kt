package com.uzuu.customer.data.remote.dto.response

data class OrderResponseDto(
    val id: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,   // PENDING | PAID | FAILED
    val orderStatus: String,     // PENDING | CONFIRMED | CANCELLED
    val orderDate: String,
    val paymentUrl: String? = null   // URL hoặc QR code string (PAYOS hoặc VietQR)
)