package ch.heigvd.iict.and.rest.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.Status

@Dao
interface ContactsDao {

    @Insert
    suspend fun insert(contact: Contact) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<Contact>) {
        contacts.forEach { insert(it) }
    }

    @Update
    fun update(contact: Contact)

    @Query("SELECT * FROM Contact")
    fun getAllContactsLiveData() : LiveData<List<Contact>>

    @Query("SELECT * FROM Contact WHERE id = :id")
    fun getContactById(id : Long) : LiveData<Contact?>

    @Query("SELECT * FROM Contact WHERE status IN ('NEW', 'UPDATED', 'DELETED')")
    suspend fun getContactsToSync() : List<Contact>

    @Query("SELECT * FROM Contact WHERE status != 'DELETED'")
    fun getActiveContactsLiveData(): LiveData<List<Contact>>

    // Suppression douce (soft delete)
    @Query("UPDATE Contact SET status = 'DELETED' WHERE id = :id")
    suspend fun softDelete(id: Long)

    // Suppression définitive (hard delete)
    @Query("DELETE FROM Contact WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("DELETE FROM Contact")
    fun clearAllContacts()

    @Query("SELECT COUNT(*) FROM Contact")
    fun getCount() : Int
}