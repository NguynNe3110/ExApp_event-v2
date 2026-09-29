package com.uzuu.customer.data.mapper

import com.uzuu.customer.data.remote.dto.response.BlogPostResponseDto
import com.uzuu.customer.domain.model.BlogPost

fun BlogPostResponseDto.toDomain(): BlogPost {
    return BlogPost(
        id = id,
        title = title.orEmpty(),
        slug = slug,
        summary = summary,
        content = content,
        thumbnail = thumbnail,
        authorName = authorName,
        status = status,
        publishedAt = publishedAt,
        createdAt = createdAt
    )
}
