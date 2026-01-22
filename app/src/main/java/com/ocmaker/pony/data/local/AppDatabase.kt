package com.ocmaker.pony.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ocmaker.pony.data.local.dao.UserDao
import com.ocmaker.pony.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}