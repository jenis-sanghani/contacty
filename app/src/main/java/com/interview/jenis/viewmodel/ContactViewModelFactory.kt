package com.interview.jenis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.interview.jenis.db.ContactDatabase

class ContactViewModelFactory(private val database: ContactDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}