/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : This enum class defines the synchronization status of a contact. It is used to track
 *               changes and manage synchronization between the local database and the server.
 *               - OK: The contact is synchronized with the server.
 *               - NEW: The contact is newly created locally and needs to be sent to the server.
 *               - UPDATED: The contact has been updated locally and requires synchronization.
 *               - DELETED: The contact is marked for deletion on the server.
 */

package ch.heigvd.iict.and.rest.models

enum class Status {
    OK, // Synced with the server
    NEW,
    UPDATED,
    DELETED
}
