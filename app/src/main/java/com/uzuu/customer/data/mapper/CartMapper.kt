package com.uzuu.customer.data.mapper

import com.uzuu.customer.data.local.entity.CartEntity
import com.uzuu.customer.data.local.entity.CartItemEntity
import com.uzuu.customer.data.remote.dto.response.CartItemResponseDto
import com.uzuu.customer.data.remote.dto.response.CartResponseDto
import com.uzuu.customer.domain.model.Cart
import com.uzuu.customer.domain.model.CartItem

fun CartResponseDto.toDomain(): Cart {
    return Cart(
        id          = id,
        items       = items.map { it.toDomain() },
        totalAmount = totalAmount
    )
}

fun CartItemResponseDto.toDomain(): CartItem {
    return CartItem(
        id             = id,
        ticketTypeId   = ticketTypeId,
        ticketTypeName = ticketTypeName,
        eventName      = eventName,
        quantity       = quantity,
        unitPrice      = unitPrice,
        subtotal       = subtotal
    )
}


fun Cart.toEntity(): CartEntity = CartEntity(
    id          = id,
    totalAmount = totalAmount
)

fun CartItem.toEntity(): CartItemEntity = CartItemEntity(
    id             = id,
    ticketTypeId   = ticketTypeId,
    ticketTypeName = ticketTypeName,
    eventName      = eventName,
    quantity       = quantity,
    unitPrice      = unitPrice,
    subtotal       = subtotal
)

fun CartEntity.toDomain(items: List<CartItemEntity>): Cart = Cart(
    id          = id,
    items       = items.map { it.toDomain() },
    totalAmount = totalAmount
)

fun CartItemEntity.toDomain(): CartItem = CartItem(
    id             = id,
    ticketTypeId   = ticketTypeId,
    ticketTypeName = ticketTypeName,
    eventName      = eventName,
    quantity       = quantity,
    unitPrice      = unitPrice,
    subtotal       = subtotal
)