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

    // LOCAL MODIFICATIONS

    suspend fun insert(contact: Contact) : Long {
        Log.d("Insert", "Contact inserted: $contact")
        return contactsDao.insert(contact)
    }

    suspend fun update(contact: Contact) {
        contact.status = Status.UPDATED

        Log.d("Update", "Contact updated: $contact")
        val serverContact = ServerContact.toServerContact(contact)
        Log.d("RepoUpdate", "ServerContact: $serverContact")

        val uuid = SharedPrefsManager.getUUID(context) ?: throw Exception("UUID not found. Perform enrollment first.")

        val response = ApiClient.service.updateContact(uuid, contact.remote_id!!, serverContact).execute()
        if (!response.isSuccessful) {
            Log.e("UpdateContact", "Failed response: ${response.errorBody()?.string()}")
            throw Exception("Failed to update contact (local id = ${contact.id} and remote id = ${contact.remote_id}): ${response.errorBody()?.string()}")
        }
        contact.status = Status.OK
        contactsDao.update(contact)
    }

    suspend fun hardDeleteContact(id: Long) {
        contactsDao.hardDelete(id)
    }

    suspend fun softDeleteContactById(id: Long) {
        contactsDao.softDelete(id)
    }

    fun getContactById(id: Long): LiveData<Contact?> {
        return contactsDao.getContactById(id)
    }

    // REMOTE MODIFICATIONS

    // First contact with the API to get a new UUID and fetch all contacts in remote db
    suspend fun enroll() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Appel à l'API pour obtenir un nouvel UUID
                val enrollResponse = ApiClient.service.enroll().execute()
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
                Log.i("Enroll", "Imported ${contacts.size} contacts")

                // 5. Insertion des contacts récupérés dans la base de données locale
                contacts.forEach{ contact ->
                    val localContact = ServerContact.toContact(contact)
                    contactsDao.insert(localContact)
                }

            } catch (e: Exception) {
                throw Exception("Enrollment failed: ${e.message}")
            }
        }
    }

    // Synchronising local db with remote db
    suspend fun refresh(){

        Log.d("Sync", "Refreshing contacts...")
        withContext(Dispatchers.IO) {
            try {

                Log.d("Sync", "Getting contacts to sync...")
                val contactsToSync = contactsDao.getContactsToSync()

                if (contactsToSync.isEmpty()){
                    Log.d("Sync", "Contacts to sync list is empty...")
                } else {
                    Log.d("Sync", "${contactsToSync.size} contacts to sync")

                    val uuid = SharedPrefsManager.getUUID(context) ?: throw Exception("UUID not found. Perform enrollment first.")
                    Log.d("Sync", "Current UUID is $uuid")

                    // New contacts sync
                    Log.d("Sync", "Synchronizing new contacts...")
                    contactsToSync.filter { it.status == Status.NEW }.forEach { contact ->
                        val serverContact = ServerContact.toServerContact(contact)

                        // TODO FIX: Sending null for birthday
                        serverContact.birthday = null
                        Log.d("Sync", "Creating new contact: $serverContact")

                        val response = ApiClient.service.createContact(uuid, serverContact).execute()
                        if (!response.isSuccessful) {
                            throw Exception("Failed to create new contact (local id = ${contact.id}) and remote id = ${contact.remote_id}): ${response.errorBody()?.string()}")
                        }
                        // Update the local contact with the remote id
                        contact.remote_id = response.body()?.id
                        contact.status = Status.OK
                        contactsDao.update(contact)
                    }

                    // Updated contacts sync
                    Log.d("Sync", "Synchronizing updated contacts...")
                    contactsToSync.filter { it.status == Status.UPDATED }.forEach { contact ->
                        val serverContact = ServerContact.toServerContact(contact)
                        val response = ApiClient.service.updateContact(uuid, contact.id!!, serverContact).execute()
                        if (!response.isSuccessful) {
                            throw Exception("Failed to update contact (local id = ${contact.id} and remote id = ${contact.remote_id}): ${response.errorBody()?.string()}")
                        }
                        contact.status = Status.OK
                        contactsDao.update(contact)
                    }

                    // Deleted contacts sync
                    Log.d("Sync", "Synchronizing deleted contacts...")
                    contactsToSync.filter { it.status == Status.DELETED }.forEach { contact ->
                        val response = ApiClient.service.deleteContact(uuid, contact.id!!).execute()
                        if (!response.isSuccessful) {
                            throw Exception("Failed to delete contact (local id = ${contact.id}) and remote id = ${contact.remote_id}): ${response.errorBody()?.string()}")
                        }
                        contactsDao.hardDelete(contact.id!!)
                    }
                }
            Log.d("Sync", "Synchronization done.")

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