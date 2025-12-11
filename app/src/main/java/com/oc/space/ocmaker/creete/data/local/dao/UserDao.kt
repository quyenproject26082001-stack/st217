package com.oc.space.ocmaker.creete.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.oc.space.ocmaker.creete.data.local.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAllUser()

    @Insert
    suspend fun insertUser(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)

}