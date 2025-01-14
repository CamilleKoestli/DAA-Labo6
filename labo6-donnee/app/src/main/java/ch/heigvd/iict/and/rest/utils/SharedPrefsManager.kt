/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : The SharedPrefsManager class provides utility methods for managing shared preferences.
 *               - Stores and retrieves a unique UUID for identifying the client in the app's local storage.
 *               - Offers methods to set, get, and clear the UUID in a private shared preferences file.
 *               - Facilitates persistent storage of data that must persist across app restarts.
 */

package ch.heigvd.iict.and.rest.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefsManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_UUID = "uuid"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getUUID(context: Context): String? {
        return getPreferences(context).getString(KEY_UUID, null)
    }

    fun setUUID(context: Context, uuid: String) {
        getPreferences(context).edit().putString(KEY_UUID, uuid).apply()
    }

    fun clearUUID(context: Context) {
        getPreferences(context).edit().remove(KEY_UUID).apply()
    }
}
