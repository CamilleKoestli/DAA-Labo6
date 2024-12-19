package ch.heigvd.iict.and.rest.repository

import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact

class ContactsRepository(private val contactsDao: ContactsDao) {

    val allContacts = contactsDao.getAllContactsLiveData()

    companion object {
        private val TAG = "ContactsRepository"
    }

    // Insère un contact dans la base de données locale
    suspend fun insert(contact: Contact) {
        contactsDao.insert(contact)
    }

}