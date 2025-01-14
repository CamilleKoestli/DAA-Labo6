/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : The ContactsRepository class acts as a central data manager between the local database and the remote server API.
 *               - Provides methods to perform CRUD operations on the local database and synchronize them with the remote API.
 *               - Handles enrollment to fetch an initial set of contacts and assigns a unique UUID for the client.
 *               - Manages contact synchronization by ensuring local changes are reflected on the server and vice versa.
 *               - Uses coroutines to handle database and network operations asynchronously.
 */

package ch.heigvd.iict.and.rest.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.ServerContact
import ch.heigvd.iict.and.rest.models.Status
import ch.heigvd.iict.and.rest.utils.SharedPrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(
    private val contactsDao: ContactsDao,
    private val context: Context
) {

    fun getAllContacts() = contactsDao.getAllContactsLiveData()

    fun getActiveContacts() = contactsDao.getActiveContactsLiveData()

    companion object {
        private val TAG = "ContactsRepository"
    }

    suspend fun insert(contact: Contact): Long {
        return withContext(Dispatchers.IO) {

            val local_id = contactsDao.insert(contact)

            val uuid = SharedPrefsManager.getUUID(context)
            val serverContact = ServerContact.toServerContact(contact)

            val response = ApiClient.service.createContact(uuid!!, serverContact)

            contact.id = local_id
            contact.remote_id = response.id
            contact.status = Status.OK
            contactsDao.update(contact)

            local_id
        }
    }

    suspend fun update(contact: Contact) {
        return withContext(Dispatchers.IO) {
            contact.status = Status.UPDATED

            val serverContact = ServerContact.toServerContact(contact)

            val uuid = SharedPrefsManager.getUUID(context)
                ?: throw Exception("UUID not found. Perform enrollment first.")

            val response =
                ApiClient.service.updateContact(uuid, contact.remote_id!!, serverContact)

            contact.status = Status.OK
            contactsDao.update(contact)
        }
    }

    suspend fun hardDeleteContact(localId: Long, remoteId: Long) {
        contactsDao.softDelete(localId)

        val uuid = SharedPrefsManager.getUUID(context)
            ?: throw Exception("UUID not found. Perform enrollment first.")
        val response = ApiClient.service.deleteContact(uuid, remoteId)

        if (response.isSuccessful) {
            contactsDao.hardDelete(localId)
        }
    }

    fun getContactById(id: Long): LiveData<Contact?> {
        return contactsDao.getContactById(id)
    }

    // First contact with the API to get a new UUID and fetch all contacts in remote db
    suspend fun enroll() {
        withContext(Dispatchers.IO) {
            try {
                // Get a new UUID from the server
                val enrollResponse = ApiClient.service.enroll().execute()
                if (!enrollResponse.isSuccessful) {
                    throw Exception("Failed to enroll: ${enrollResponse.errorBody()?.string()}")
                }

                val newUUID = enrollResponse.body() ?: throw Exception("Empty UUID response")

                // Save the UUID in shared preferences
                SharedPrefsManager.setUUID(context, newUUID)

                // Clear the local database from all contacts
                contactsDao.clearAllContacts()

                // Fetch all contacts from the server linked to the new UUID
                val response = ApiClient.service.getAllContacts(newUUID).execute()

                if (!response.isSuccessful) {
                    throw Exception("Failed to fetch contacts: ${response.errorBody()?.string()}")
                }
                val contacts = response.body() ?: emptyList()

                // Insert the contacts in the local database
                contacts.forEach { contact ->
                    val localContact = ServerContact.toContact(contact)
                    contactsDao.insert(localContact)
                }

            } catch (e: Exception) {
                throw Exception("Enrollment failed: ${e.message}")
            }
        }
    }

    // Synchronising local db with remote db
    suspend fun refresh() {

        withContext(Dispatchers.IO) {
            try {

                val contactsToSync = contactsDao.getContactsToSync()

                if (!contactsToSync.isEmpty()) {

                    val uuid = SharedPrefsManager.getUUID(context)
                        ?: throw Exception("UUID not found. Perform enrollment first.")

                    // New contacts sync
                    contactsToSync.filter { it.status == Status.NEW }.forEach { contact ->
                        val serverContact = ServerContact.toServerContact(contact)

                        // Sending null for birthday for simplification
                        serverContact.birthday = null

                        val response = ApiClient.service.createContact(uuid, serverContact)

                        contact.remote_id = response.id
                        contact.status = Status.OK
                        contactsDao.update(contact)
                    }

                    // Updated contacts sync

                    contactsToSync.filter { it.status == Status.UPDATED }.forEach { contact ->
                        val serverContact = ServerContact.toServerContact(contact)
                        val response =
                            ApiClient.service.updateContact(uuid, contact.id!!, serverContact)

                        contact.status = Status.OK
                        contactsDao.update(contact)
                    }

                    // Deleted contacts sync
                    contactsToSync.filter { it.status == Status.DELETED }.forEach { contact ->
                        val response = ApiClient.service.deleteContact(uuid, contact.id!!)
                        if (!response.isSuccessful) {
                            throw Exception(
                                "Failed to delete contact (local id = ${contact.id}) and remote id = ${contact.remote_id}): ${
                                    response.errorBody()?.string()
                                }"
                            )
                        }
                        contactsDao.hardDelete(contact.id!!)
                    }
                } else {
                    Log.d(TAG, "No contacts to synchronize")
                }

            } catch (e: Exception) {
                Log.e("SyncError", "Failed to synchronize: ${e.message}")
            }
        }
    }

}