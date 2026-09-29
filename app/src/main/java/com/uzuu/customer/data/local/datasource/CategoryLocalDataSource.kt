package com.uzuu.customer.data.local.datasource

import com.uzuu.customer.data.local.dao.CategoryDao
import com.uzuu.customer.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryLocalDataSource @Inject constructor(private val categoryDao: CategoryDao) {
    
    fun observeAllCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAllCategories()

    suspend fun getAllCategories(): List<CategoryEntity> = categoryDao.getAllCategories()
    
    suspend fun getCategoryById(id: Int): CategoryEntity? = categoryDao.getCategoryById(id)

    suspend fun cacheCategories(categories: List<CategoryEntity>) {
        categoryDao.insertCategories(categories)
    }
    
    suspend fun clearAllCategories() {
        categoryDao.deleteAllCategories()
    }
    
    suspend fun hasCachedData(): Boolean = categoryDao.getCategoryCount() > 0
}
