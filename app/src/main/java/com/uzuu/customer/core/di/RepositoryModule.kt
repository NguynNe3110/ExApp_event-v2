package com.uzuu.customer.core.di

import com.uzuu.customer.data.repository.AuthRepositoryImpl
import com.uzuu.customer.data.repository.BlogRepositoryImpl
import com.uzuu.customer.data.repository.CartRepositoryImpl
import com.uzuu.customer.data.repository.CategoryRepositoryImpl
import com.uzuu.customer.data.repository.EventRepositoryImpl
import com.uzuu.customer.data.repository.MyTicketRepositoryImpl
import com.uzuu.customer.data.repository.OrderRepositoryImpl
import com.uzuu.customer.data.repository.UserRepositoryImpl
import com.uzuu.customer.data.repository.VoucherRepositoryImpl
import com.uzuu.customer.domain.repository.AuthRepository
import com.uzuu.customer.domain.repository.BlogRepository
import com.uzuu.customer.domain.repository.CartRepository
import com.uzuu.customer.domain.repository.CategoryRepository
import com.uzuu.customer.domain.repository.EventRepository
import com.uzuu.customer.domain.repository.MyTicketRepository
import com.uzuu.customer.domain.repository.OrderRepository
import com.uzuu.customer.domain.repository.UserRepository
import com.uzuu.customer.domain.repository.VoucherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBlogRepository(
        impl: BlogRepositoryImpl
    ): BlogRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository

    @Binds
    @Singleton
    abstract fun bindMyTicketRepository(
        impl: MyTicketRepositoryImpl
    ): MyTicketRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindVoucherRepository(
        impl: VoucherRepositoryImpl
    ): VoucherRepository
}
