package com.uzuu.customer.core.di

import android.content.Context
import com.uzuu.customer.data.local.AppDatabase
import com.uzuu.customer.data.local.dao.CartDao
import com.uzuu.customer.data.local.dao.CategoryDao
import com.uzuu.customer.data.local.dao.EventDao
import com.uzuu.customer.data.local.dao.OrderDao
import com.uzuu.customer.data.local.dao.TicketDao
import com.uzuu.customer.data.local.dao.UsersDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.get(context)
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UsersDao = db.userDao()

    @Provides
    fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()

    @Provides
    fun provideTicketDao(db: AppDatabase): TicketDao = db.ticketDao()

    @Provides
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao()

    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
}
