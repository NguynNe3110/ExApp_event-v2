package com.uzuu.customer.data.mapper

import com.uzuu.customer.data.local.entity.CategoryEntity
import com.uzuu.customer.domain.model.CategoryItem

fun CategoryEntity.toDomain(isSelected: Boolean = false): CategoryItem {
    return CategoryItem(
        id = id,
        name = name,
        isSelected = isSelected
    )
}

fun CategoryItem.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name
    )
}
