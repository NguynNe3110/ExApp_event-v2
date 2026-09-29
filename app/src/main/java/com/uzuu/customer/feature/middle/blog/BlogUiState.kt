package com.uzuu.customer.feature.middle.blog

import com.uzuu.customer.domain.model.BlogPost

data class BlogUiState(
    val isLoading: Boolean = false,
    val blogs: List<BlogPost> = emptyList(),
    val error: String? = null
)
