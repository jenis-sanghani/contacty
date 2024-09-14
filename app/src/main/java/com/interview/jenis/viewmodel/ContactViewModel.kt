package com.interview.jenis.viewmodel

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interview.jenis.db.ContactDatabase
import com.interview.jenis.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel(private val database: ContactDatabase) : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    private val contacts: LiveData<List<Contact>> = _contacts

    fun fetchAndSaveContacts(contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            val fetchedContacts = fetchDeviceContacts(contentResolver)
            database.contactDao().insertContacts(fetchedContacts)
            loadAllContacts()
        }
    }

    private suspend fun loadAllContacts() {
        val contactsFromDB = database.contactDao().getAllContacts()
        _contacts.postValue(contactsFromDB)
    }

    fun getAllContacts(): LiveData<List<Contact>> = contacts

    fun saveContacts(contacts: List<Contact>) {
        viewModelScope.launch(Dispatchers.IO) {
            database.contactDao().insertContacts(contacts)
        }
    }

    suspend fun searchContactByPhone(query: String): List<Contact> {
        return database.contactDao().searchContactsByPhone(query)
    }

    fun fetchDeviceContacts(contentResolver: ContentResolver): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val phoneNumber = it.getString(numberIndex)
                contacts.add(Contact(name = name, phoneNumber = phoneNumber))
            }
        }

        return contacts
    }
}