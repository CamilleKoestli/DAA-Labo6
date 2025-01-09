package ch.heigvd.iict.and.rest.repository

import android.content.Context
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.utils.SharedPrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                // Obtenir un nouvel UUID depuis le serveur
                val newUUID = ApiClient.service.enroll()

                // Effacer la base de données locale
                contactsDao.clearAllContacts()

                // Sauvegarder l'UUID dans SharedPreferences
                SharedPrefsManager.setUUID(context, newUUID.toString())

                // Récupérer les contacts depuis le serveur
                val serverContactsResponse = ApiClient.service.getAllContacts().execute()

                if (!serverContactsResponse.isSuccessful) {
                    throw Exception("Failed to fetch contacts: ${serverContactsResponse.errorBody()}")
                }

                // Insérer les contacts dans la base locale
                contactsDao.insertAll(serverContactsResponse.body()!!)

            } catch (e: Exception) {
                // Gérer les erreurs réseau
                throw Exception("Enrollment failed: ${e.message}")
            }
        }
    }

}