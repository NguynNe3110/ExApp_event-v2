package com.uzuu.customer.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uzuu.customer.data.local.entity.EventEntity
import com.uzuu.customer.data.remote.dto.response.EventResponseDto
import com.uzuu.customer.domain.model.Event

private val gson = Gson()

fun EventResponseDto.eventDtoToDomain(): Event {
    return Event(
        id            = id,
        categoryId    = category?.id ?: -1,
        name          = name.orEmpty(),
        categoryName  = categoryName ?: category?.name.orEmpty(),
        location      = listOfNotNull(location, address, provinceName ?: province)
            .firstOrNull { it.isNotBlank() }
            .orEmpty(),
        startTime     = startTime,
        endTime       = endTime,
        saleStartDate = saleStartDate,
        saleEndDate   = saleEndDate,
        description   = description,
        status        = status.orEmpty(),
        imageUrls     = imageUrls.orEmpty(),
        ticketTypes   = ticketTypes.orEmpty().map { it.ticketDtoToDomain() },
        organizerName = organizerName ?: organizer?.fullName
    )
}

fun EventEntity.toDomain(): Event {
    val imageList: List<String> = try {
        if (imageUrls.isBlank()) emptyList()
        else gson.fromJson(imageUrls, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    val ticketList: List<com.uzuu.customer.domain.model.CategoryTicket> = try {
        if (ticketTypes.isBlank()) emptyList()
        else gson.fromJson(ticketTypes, object : TypeToken<List<com.uzuu.customer.domain.model.CategoryTicket>>() {}.type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    return Event(
        id            = id,
        categoryId    = categoryId,
        name          = name,
        categoryName  = categoryName,
        location      = location,
        startTime     = startTime,
        endTime       = endTime,
        saleStartDate = saleStartDate,
        saleEndDate   = saleEndDate,
        description   = description,
        status        = status,
        imageUrls     = imageList,
        ticketTypes   = ticketList,
        organizerName = null
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id            = id,
        categoryId    = categoryId,
        name          = name,
        categoryName  = categoryName,
        location      = location,
        startTime     = startTime,
        endTime       = endTime,
        saleStartDate = saleStartDate,
        saleEndDate   = saleEndDate,
        description   = description,
        status        = status,
        imageUrls     = gson.toJson(imageUrls),
        ticketTypes   = gson.toJson(ticketTypes)
    )
}
