import ch.heigvd.iict.and.rest.models.Contact
import retrofit2.Call
import retrofit2.http.*

interface ContactsApiService {

    @GET("/enroll")
    suspend fun enroll(): Call<String> // Returns a UUID

    @GET("/contacts")
    suspend fun getAllContacts(@Header("X-UUID") uuid: String): Call<List<Contact>>

    @GET("/contacts/{id}")
    suspend fun getContactById(
        @Path("id") id: Long,
        @Header("X-UUID") uuid: String
    ): Call<Contact>

    @POST("/contacts")
    suspend fun createContact(
        @Header("X-UUID") uuid: String,
        @Body contact: Contact): Call<Contact>

    @PUT("/contacts/{id}")
    suspend fun updateContact(@Path("id") id: Long, @Body contact: Contact): Call<Contact>

    @DELETE("/contacts/{id}")
    suspend fun deleteContact(@Path("id") id: Long): Call<Void>
}
