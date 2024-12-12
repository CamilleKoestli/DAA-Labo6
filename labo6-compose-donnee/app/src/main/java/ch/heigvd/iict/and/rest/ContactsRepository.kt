package ch.heigvd.iict.and.rest

import ch.heigvd.iict.and.rest.database.ContactsDao

class ContactsRepository(private val contactsDao: ContactsDao) {

    val allContacts = contactsDao.getAllContactsLiveData()

}