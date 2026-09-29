package com.uzuu.customer.domain.model

data class BlogPost(
    val id: Long,
    val title: String,
    val slug: String?,
    val summary: String?,
    val content: String?,
    val thumbnail: String?,
    val authorName: String?,
    val status: String?,
    val publishedAt: String?,
    val createdAt: String?
)
