/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : This class provides type converters for the Calendar object to be used with Room.
 */

package ch.heigvd.iict.and.rest.database.converters

import androidx.room.TypeConverter
import java.util.*

class CalendarConverter {

    @TypeConverter
    fun toCalendar(dateLong: Long) =
        Calendar.getInstance().apply {
            time = Date(dateLong)
        }

    @TypeConverter
    fun fromCalendar(date: Calendar) =
        date.time.time

}