/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : The ContactsViewModel class provides a bridge between the ContactsRepository and the UI layer.
 *               - It manages the lifecycle-aware data fetching for the UI, ensuring proper handling of LiveData.
 *               - Exposes CRUD operations and synchronization methods to interact with the repository.
 *               - Uses coroutines in the ViewModel scope to perform background tasks such as network requests and database operations.
 *               - Implements a factory class for creating instances of the ViewModel with the required repository dependency.
 */

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
                val insertedId = repository.insert(contact) // Return the inserted ID
                onResult(insertedId) // Call the callback function
            } catch (e: Exception) {
                Log.e("Insert error", "Error inserting contact", e)
            }
        }
    }


    fun updateContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            if (contact.remote_id == null){
                var remote_id = repository.insert(contact)
                contact.remote_id = remote_id
            } else {
                repository.update(contact)
            }
        }
    }

    fun deleteContact(localId: Long, remoteId: Long) {
        viewModelScope.launch {
            try {
                repository.hardDeleteContact(localId, remoteId)
            } catch (e: Exception) {
                Log.d("Contact VM delete contact", "Error hard deleting contact", e)
            }
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