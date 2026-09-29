package com.uzuu.customer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uzuu.customer.data.local.entity.UsersEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface UsersDao {
    @Query("select * from users order by id asc")
    fun observeUser(): Flow<List<UsersEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createUser(user: UsersEntity) : Long

    @Update
    suspend fun updateUser(user: UsersEntity) : Int

    @Query("delete from users where id = :id")
    suspend fun deleteUserById(id: Int): Int

    //
    @Query("delete from users where username = :username")
    suspend fun deleteUserByUsername(username: String): Int

    @Query("select * from users where username = :username limit 1")
    suspend fun getUserByUsername(username: String) : UsersEntity

    //
    @Query("select exists(select 1 from users where id = :id)")
    suspend fun checkUserExists(id: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun isUserExist(username: String): Boolean
}