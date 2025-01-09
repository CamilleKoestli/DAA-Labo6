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
                // TODO
                // Gérez les erreurs d'enrollment ici (par ex., afficher un Toast dans l'UI)
                println("Enrollment error: ${e.message}")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            // TODO
        }
    }

    fun insert(newContact: Contact) {
        viewModelScope.launch {
            repository.insert(newContact)
        }
    }

    // TODO vérifier
    private val _selectedContact = MutableLiveData<Contact?>()
    val selectedContact: LiveData<Contact?> get() = _selectedContact

    fun selectContact(contact: Contact?) {
        _selectedContact.value = contact
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