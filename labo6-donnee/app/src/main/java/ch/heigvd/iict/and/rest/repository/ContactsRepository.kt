package ch.heigvd.iict.and.rest.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.Status
import ch.heigvd.iict.and.rest.utils.SharedPrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ContactsRepository(
    private val contactsDao: ContactsDao,
    private val context: Context
) {

    fun getAllContacts() = contactsDao.getAllContactsLiveData()

    companion object {
        private val TAG = "ContactsRepository"
    }

    // LOCAL MODIFICATIONS

    suspend fun insert(contact: Contact): Long {
        return contactsDao.insert(contact)
    }

    suspend fun update(contact: Contact) {
        contactsDao.update(contact)
    }

    suspend fun softDeleteContactById(id: Long) {
        contactsDao.softDelete(id)
    }

    suspend fun hardDeleteContact(contact: Contact) {
        contactsDao.hardDelete(contact)
    }

    suspend fun hardDeleteContactById(id: Long) {
        contactsDao.hardDeleteById(id)
    }

    fun getContactById(contactId: Long) = contactsDao.getContactById(contactId)

    // REMOTE MODIFICATIONS

    // First contact with the API to get a new UUID and fetch all contacts in remote db
    suspend fun enroll() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Appel à l'API pour obtenir un nouvel UUID
                val enrollResponse: Response<String> = ApiClient.service.enroll().execute()
                if (!enrollResponse.isSuccessful) {
                    throw Exception("Failed to enroll: ${enrollResponse.errorBody()?.string()}")
                }
                val newUUID = enrollResponse.body() ?: throw Exception("Empty UUID response")
                Log.d("Enroll", "new UUID: $newUUID")

                // 2. Sauvegarde de l'UUID dans les SharedPreferences
                SharedPrefsManager.setUUID(context, newUUID)

                // 3. Suppression des contacts locaux
                contactsDao.clearAllContacts()

                // 4. Récupération des contacts associés à l'UUID
                val response = ApiClient.service.getAllContacts(newUUID).execute()
                Log.d("Enroll", "contactsResponse: $response")

                if (!response.isSuccessful) {
                    throw Exception("Failed to fetch contacts: ${response.errorBody()?.string()}")
                }
                val contacts = response.body() ?: emptyList()

                // Set new contact status to OK
                contacts.forEach{ contact ->
                    contact.status = Status.OK
                }

                // 5. Insertion des contacts récupérés dans la base de données locale
                contactsDao.insertAll(contacts)

            } catch (e: Exception) {
                // Gérer les erreurs réseau
                throw Exception("Enrollment failed: ${e.message}")
            }
        }
    }

    // Synchronising local db with remote db
    suspend fun refresh(){
        withContext(Dispatchers.IO) {
            try {
                val contactsToSync = contactsDao.getContactsToSync()
//                val contactsToSync = contactsDao.getContactsToSync().observe(this, Observer{
//                    contact ->
//                }
//                )

                if (contactsToSync.isEmpty()){
                    Log.d("Sync", "Contacts to sync list is empty...")
                }

                val uuid = SharedPrefsManager.getUUID(context) ?: throw Exception("UUID not found. Perform enrollment first.")

                // New contacts sync
                contactsToSync.filter { it.status == Status.NEW }.forEach { contact ->
                    val response = ApiClient.service.createContact(uuid, contact).execute()
                    if (!response.isSuccessful) {
                        throw Exception("Failed to create new contact (local id = ${contact.id}) and remote id = ${contact.remote_id}): ${response.errorBody()?.string()}")
                    }
                    contact.id = response.body()?.id
                    contact.status = Status.OK
                    contactsDao.update(contact)
                }

                // Updated contacts sync
                contactsToSync.filter { it.status == Status.UPDATED }.forEach { contact ->
                    val response = ApiClient.service.updateContact(contact.id!!, contact).execute()
                    if (!response.isSuccessful) {
                        throw Exception("Failed to update contact (local id = ${contact.id} and remote id = ${contact.remote_id}): ${response.errorBody()?.string()}")
                    }
                    contact.status = Status.OK
                    contactsDao.update(contact)
                }

                // Deleted contacts sync
                contactsToSync.filter { it.status == Status.DELETED }.forEach { contact ->
                    val response = ApiClient.service.deleteContact(contact.id!!).execute()
                    if (!response.isSuccessful) {
                        throw Exception("Failed to delete contact (local id = ${contact.id}) and remote id = ${contact.remote_id}): ${response.errorBody()?.string()}")
                    }
                    contactsDao.hardDelete(contact)
                }
            } catch (e: Exception) {
                // Gérer les erreurs réseau
                Log.e("SyncError", "Failed to synchronize: ${e.message}")
            }
        }
    }


    // TODO voir si necessaire
//    suspend fun getContact(id: Long): Contact? {
//        return withContext(Dispatchers.IO) {
//            try {
//                val uuid = SharedPrefsManager.getUUID(context) // Récupérer le UUID
//                if (uuid == null) {
//                    throw Exception("UUID not found. Perform enrollment first.")
//                }
//
//                val response = ApiClient.service.getContactById(id, uuid).execute()
//                if (response.isSuccessful) {
//                    response.body() // Retourne le contact si tout est OK
//                } else {
//                    throw Exception("Failed to fetch contact: ${response.code()}")
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        }
//    }

}