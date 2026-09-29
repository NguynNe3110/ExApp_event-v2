package com.uzuu.customer.data.local.datasource

import com.uzuu.customer.data.local.dao.UsersDao
import com.uzuu.customer.data.local.entity.UsersEntity


import javax.inject.Inject

class UserDataLocalSource @Inject constructor(
    private val usersDao: UsersDao
) {
    fun observeUser() = usersDao.observeUser()

    suspend fun createUser(user: UsersEntity): Long {
        return usersDao.createUser(user)
    }

    suspend fun updateUser(user: UsersEntity) : Int {
        return usersDao.updateUser(user)
    }

    suspend fun deleteUserById(id: Int) : Int {
        return usersDao.deleteUserById(id)
    }

    suspend fun getUserByUsername(username: String) : UsersEntity {
        return usersDao.getUserByUsername(username)
    }

    suspend fun isUserExist(username: String): Boolean {
        return usersDao.isUserExist(username)
    }
}