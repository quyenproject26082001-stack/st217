package com.oc.space.ocmaker.creete.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oc.space.ocmaker.creete.data.local.dao.UserDao
import com.oc.space.ocmaker.creete.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}