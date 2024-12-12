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