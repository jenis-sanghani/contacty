package com.interview.jenis.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.interview.jenis.model.Contact

@Dao
interface ContactDao {
    @Insert
    suspend fun insertContacts(contacts: List<Contact>)

    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<Contact>

    @Query("SELECT * FROM contacts WHERE phoneNumber LIKE :searchQuery")
    suspend fun searchContactsByPhone(searchQuery: String): List<Contact>
}