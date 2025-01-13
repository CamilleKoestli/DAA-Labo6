package ch.heigvd.iict.and.rest.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.heigvd.iict.and.rest.repository.ContactsRepository
import ch.heigvd.iict.and.rest.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    val allContacts = repository.getAllContacts()

    val activeContacts: LiveData<List<Contact>> = repository.getActiveContacts()


    // actions
    fun enroll() {
        viewModelScope.launch {
            try {
                repository.enroll()
            } catch (e: Exception) {
                println("Enrollment error: ${e.message}")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                repository.refresh()
            } catch (e: Exception) {
                println("Refresh error: ${e.message}")
            }
        }
    }

    fun insert(contact: Contact, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val insertedId = repository.insert(contact) // Retourne l'ID
                onResult(insertedId) // Appelez le callback avec l'ID
            } catch (e: Exception) {
                Log.e("Insert error", "Error inserting contact", e)
            }
        }
    }


    fun updateContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(contact)
        }
    }

    fun softDeleteContactById(id: Long) {
        viewModelScope.launch (Dispatchers.IO) {
            try {
                repository.softDeleteContactById(id)
            } catch (e: Exception) {
                // Log error or show Toast
                println("Error performing soft delete: ${e.message}")
            }
        }
    }

    fun hardDeleteContact(id: Long) {
        viewModelScope.launch {
            try {
                repository.hardDeleteContact(id)
            } catch (e: Exception) {
                // Log error or show Toast
                println("Error performing hard delete: ${e.message}")
            }
        }
    }

    fun getContactById(contactId: Long) = repository.getContactById(contactId)

}

class ContactsViewModelFactory(private val repository: ContactsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}