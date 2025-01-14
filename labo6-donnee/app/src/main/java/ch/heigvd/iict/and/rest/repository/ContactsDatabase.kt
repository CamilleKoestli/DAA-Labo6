/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : This class represents the Room database for storing contact information.
 *               - It defines a single entity, `Contact`, and its associated DAO, `ContactsDao`.
 *               - Utilizes the `CalendarConverter` for handling `Calendar` type conversions.
 *               - Implements a singleton pattern to ensure a single instance of the database is used throughout the app.
 *               - Includes `fallbackToDestructiveMigration` for handling schema changes by resetting the database.
 */

package ch.heigvd.iict.and.rest.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.database.converters.CalendarConverter
import ch.heigvd.iict.and.rest.models.Contact

@Database(entities = [Contact::class], version = 8, exportSchema = true)
@TypeConverters(CalendarConverter::class)
abstract class ContactsDatabase : RoomDatabase() {

    abstract fun contactsDao() : ContactsDao

    companion object {

        @Volatile
        private var INSTANCE : ContactsDatabase? = null

        fun getDatabase(context: Context) : ContactsDatabase {

            return INSTANCE ?: synchronized(this) {
                val _instance = Room.databaseBuilder(context.applicationContext,
                ContactsDatabase::class.java, "contacts.db")
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = _instance
                _instance
            }
        }

    }

}