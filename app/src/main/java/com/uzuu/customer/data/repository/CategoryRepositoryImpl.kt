package com.uzuu.customer.data.repository

import com.uzuu.customer.data.local.datasource.CategoryLocalDataSource
import com.uzuu.customer.data.mapper.toDomain
import com.uzuu.customer.data.mapper.toEntity
import com.uzuu.customer.data.remote.datasource.CategoryRemoteDataSource
import com.uzuu.customer.domain.model.CategoryItem
import com.uzuu.customer.domain.repository.CategoryRepository
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val remote: CategoryRemoteDataSource,
    private val local: CategoryLocalDataSource
) : CategoryRepository {
    override suspend fun getAllCategories(): List<CategoryItem> {
        val cachedCategories = getCachedCategories()
        if (cachedCategories.isNotEmpty()) {
            println("DEBUG [CategoryRepositoryImpl] Returning ${cachedCategories.size} cached categories")
            return cachedCategories
        }

        return try {
            val response = remote.getAllCategories()
            val allCategory = CategoryItem(id = -1, name = "Tất cả", isSelected = true)
            val fromServer = response.result.map {
                CategoryItem(
                    id = it.id.toInt(),
                    name = it.name,
                    isSelected = false
                )
            }
            val result = listOf(allCategory) + fromServer
            
            val entities = fromServer.map { it.toEntity() }
            local.cacheCategories(entities)
            println("DEBUG [CategoryRepositoryImpl] Cached ${fromServer.size} categories")
            
            result
        } catch (e: Exception) {
            println("DEBUG [CategoryRepositoryImpl] Error fetching categories: ${e.message}")
            val cachedCategories = getCachedCategories()
            if (cachedCategories.isNotEmpty()) {
                val allCategory = CategoryItem(id = -1, name = "Tất cả", isSelected = true)
                listOf(allCategory) + cachedCategories
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getCachedCategories(): List<CategoryItem> {
        return try {
            local.getAllCategories().map { it.toDomain(isSelected = false) }
        } catch (e: Exception) {
            println("DEBUG [CategoryRepositoryImpl] Error getting cached categories: ${e.message}")
            emptyList()
        }
    }
}