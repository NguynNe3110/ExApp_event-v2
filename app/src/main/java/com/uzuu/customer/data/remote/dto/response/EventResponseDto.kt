package com.uzuu.customer.data.remote.dto.response

data class EventResponseDto(
    val id: Long,
    val name: String?,
    val categoryName: String?,
    val category: CategoryEventResponseDto?,
    val organizerName: String?,
    val organizer: OrganizerResponseDto?,
    val location: String?,
    val province: String?,
    val provinceName: String?,
    val address: String?,
    val startTime: String?,
    val endTime: String?,
    val saleStartDate: String?,
    val saleEndDate: String?,
    val description: String?,
    val status: String?,
    val imageUrls: List<String>?,
    val ticketTypes: List<CategoryTicketResponseDto>?,
    val createdAt: String?,
    val updatedAt: String?
)

data class OrganizerResponseDto(
    val id: Long?,
    val fullName: String?,
    val email: String?
)
