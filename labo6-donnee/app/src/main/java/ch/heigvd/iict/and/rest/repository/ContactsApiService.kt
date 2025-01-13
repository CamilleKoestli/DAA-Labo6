import ch.heigvd.iict.and.rest.models.ServerContact
import retrofit2.Call
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
    fun createContact(
        @Header("X-UUID") uuid: String,
        @Body contact: ServerContact): Call<ServerContact>

    @PUT("/contacts/{id}")
    fun updateContact(@Header("X-UUID") uuid: String,
                      @Path("id") remoteId: Long,
                      @Body contact: ServerContact): Call<ServerContact>

    @DELETE("/contacts/{id}")
    fun deleteContact(@Header("X-UUID") uuid: String,
                      @Path("id") remoteId: Long): Call<Void>
}
