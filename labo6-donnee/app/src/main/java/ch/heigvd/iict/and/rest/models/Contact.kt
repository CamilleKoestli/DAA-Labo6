/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : This data class represents a Contact entity in the local database. It includes both
 *               local attributes (e.g., id, status) and attributes synchronized with the remote server
 *               (e.g., remote_id).
 */

package ch.heigvd.iict.and.rest.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Contact(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var remote_id: Long? = null, // Remote ID from the server
    var name: String,
    var firstname: String?,
    var birthday: String?,
    var email: String?,
    var address: String?,
    var zip: String?,
    var city: String?,
    var type: PhoneType?,
    var phoneNumber: String?,
    var status: Status
)