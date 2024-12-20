package com.example.hook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hook.data.local.entity.ContactEntity

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveContact(contact: ContactEntity)

    @Query("SELECT * FROM contacts WHERE userId = :userId")
    suspend fun getContactById(userId: String): ContactEntity?

    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<ContactEntity>

    @Delete
    suspend fun deleteContact(contact: ContactEntity)
}