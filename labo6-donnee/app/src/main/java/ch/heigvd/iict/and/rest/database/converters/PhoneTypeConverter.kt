package ch.heigvd.iict.and.rest.models

import androidx.room.TypeConverter

class PhoneTypeConverter {
    @TypeConverter
    fun fromPhoneType(type: PhoneType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toPhoneType(value: String?): PhoneType? {
        return value?.let { PhoneType.valueOf(it) }
    }
}
