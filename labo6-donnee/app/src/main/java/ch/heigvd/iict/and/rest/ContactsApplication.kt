package ch.heigvd.iict.and.rest

import android.app.Application
import ch.heigvd.iict.and.rest.repository.ContactsDatabase
import ch.heigvd.iict.and.rest.repository.ContactsRepository

class ContactsApplication : Application() {

    private val database by lazy { ContactsDatabase.getDatabase(this) }
    val repository by lazy { ContactsRepository(database.contactsDao()) }
}