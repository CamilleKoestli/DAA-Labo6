/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : This interface defines the API endpoints for interacting with the server. It includes:
 *               - `enroll`: Retrieves a new UUID for the client.
 *               - `getAllContacts`: Fetches all contacts associated with the client's UUID.
 *               - `getContactById`: Fetches a specific contact by its ID.
 *               - `createContact`: Creates a new contact on the server.
 *               - `updateContact`: Updates an existing contact on the server using its remote ID.
 *               - `deleteContact`: Deletes a contact on the server using its remote ID.
 *
 */

import ch.heigvd.iict.and.rest.models.ServerContact
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ContactsApiService {

    @GET("/enroll")
    fun enroll(): Call<String> // Returns a UUID

    @GET("/contacts")
    fun getAllContacts(@Header("X-UUID") uuid: String): Call<List<ServerContact>>

    @GET("/contacts/{id}")
    suspend fun getContactById(
        @Path("id") id: Long,
        @Header("X-UUID") uuid: String
    ): Call<ServerContact>

    @POST("/contacts")
    suspend fun createContact(
        @Header("X-UUID") uuid: String,
        @Body contact: ServerContact): ServerContact

    @PUT("/contacts/{id}")
    suspend fun updateContact(@Header("X-UUID") uuid: String,
                      @Path("id") remoteId: Long,
                      @Body contact: ServerContact): ServerContact

    @DELETE("/contacts/{id}")
    suspend fun deleteContact(@Header("X-UUID") uuid: String,
                      @Path("id") remoteId: Long): Response<Void>
}
