package com.uzuu.customer.data.repository

import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.core.result.safeApiCall
import com.uzuu.customer.data.local.datasource.UserDataLocalSource
import com.uzuu.customer.data.mapper.userDtoToDomain
import com.uzuu.customer.data.mapper.userEntityToDomain
import com.uzuu.customer.data.mapper.userdomainToEntity
import com.uzuu.customer.data.remote.datasource.UserRemoteDataSource
import com.uzuu.customer.data.remote.dto.request.UserRequestDto
import com.uzuu.customer.data.remote.dto.response.UserResponseDto
import com.uzuu.customer.domain.model.Users
import com.uzuu.customer.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocal: UserDataLocalSource,
    private val userRemote: UserRemoteDataSource
) : UserRepository {

    override suspend fun getMyInfo(): ApiResult<UserResponseDto> =
        safeApiCall {
            val response = userRemote.getMyInfo()
            if (response.code == 200 || response.code == 0 || response.code == 1000) {
                response.result
            } else {
                throw Exception(response.message ?: "Không lấy được thông tin")
            }
        }

    override suspend fun updateInfo(
        username: String,
        request: UserRequestDto
    ): ApiResult<UserResponseDto> =
        safeApiCall {
            val response = userRemote.updateInfo(username, request)
            if (response.code == 200 || response.code == 0 || response.code == 1000) {
                response.result
            } else {
                throw Exception(response.message ?: "Cập nhật thất bại")
            }
        }

    override val users: Flow<List<Users>>
        get() = userLocal.observeUser().map { entities -> 
            entities.map { it.userEntityToDomain() }
        }

    override suspend fun createUser(user: Users): Long {
        return userLocal.createUser(user.userdomainToEntity(null))
    }

    override suspend fun updateUser(user: Users): Int {
        val existing = runCatching {
            userLocal.getUserByUsername(user.username)
        }.getOrNull()
        return userLocal.updateUser(user.userdomainToEntity(existing))
    }

    override suspend fun getUserByUsername(username: String): Users {
        return try {
            userLocal.getUserByUsername(username).userEntityToDomain()
        } catch (e: Exception) {
            val result = userRemote.getMyInfo()
            if (result.code == 200 || result.code == 0 || result.code == 1000) {
                val userDto = result.result
                val domainUser = userDto.userDtoToDomain()
                userLocal.createUser(domainUser.userdomainToEntity(null))
                domainUser
            } else {
                throw Exception("User not found")
            }
        }
    }

    override suspend fun deleteUserById(id: Int): Int {
        return userLocal.deleteUserById(id)
    }

    override suspend fun isUserExist(username: String): Boolean {
        return userLocal.isUserExist(username)
    }
}