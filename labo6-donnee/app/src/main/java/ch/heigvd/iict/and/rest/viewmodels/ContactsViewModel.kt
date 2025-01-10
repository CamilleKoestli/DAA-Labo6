package ch.heigvd.iict.and.rest.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.heigvd.iict.and.rest.repository.ContactsRepository
import ch.heigvd.iict.and.rest.models.Contact
import kotlinx.coroutines.launch

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    val allContacts = repository.allContacts

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
            // TODO
            try {
                repository.refresh()
            } catch (e: Exception) {
                println("Refresh error: ${e.message}")
            }
        }
    }

    fun insert(newContact: Contact) {
        viewModelScope.launch {
            repository.insert(newContact)
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            repository.update(contact)
        }
    }

    // TODO ptt pas necessaire ici
    fun hardDelete(contact: Contact) {
        viewModelScope.launch {
            repository.hardDelete(contact)
        }
    }

    fun softDeleteContactById(id: Long) {
        viewModelScope.launch {
            repository.softDeleteContactById(id)
        }
    }

    fun getContactById(id: Long): LiveData<Contact?> {
        return repository.getContactById(id)
    }

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