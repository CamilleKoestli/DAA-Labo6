package ch.heigvd.iict.and.rest.repository

import android.content.Context
import android.util.Log
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.utils.SharedPrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ContactsRepository(
    private val contactsDao: ContactsDao,
    private val context: Context
) {

    val allContacts = contactsDao.getAllContactsLiveData()

    companion object {
        private val TAG = "ContactsRepository"
    }

    // Insère un contact dans la base de données locale
    suspend fun insert(contact: Contact) {
        contactsDao.insert(contact)
    }

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
                val contactsResponse = ApiClient.service.getAllContacts(newUUID).execute()
                Log.d("Enroll", "contactsResponse: $contactsResponse")

                if (!contactsResponse.isSuccessful) {
                    throw Exception("Failed to fetch contacts: ${contactsResponse.errorBody()?.string()}")
                }
                val contacts = contactsResponse.body() ?: emptyList()

                // 5. Insertion des contacts récupérés dans la base de données locale
                contactsDao.insertAll(contacts)

            } catch (e: Exception) {
                // Gérer les erreurs réseau
                throw Exception("Enrollment failed: ${e.message}")
            }
        }
    }

}