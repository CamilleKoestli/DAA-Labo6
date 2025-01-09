import ch.heigvd.iict.and.rest.models.Contact
import retrofit2.Call
import retrofit2.http.*

interface ContactsApiService {

    @GET("/enroll")
    fun enroll(): Call<String> // Returns a UUID

    @GET("/contacts")
    fun getAllContacts(@Header("X-UUID") uuid: String): Call<List<Contact>>

    @GET("/contacts/{id}")
    fun getContactById(
        @Path("id") id: Long,
        @Header("X-UUID") uuid: String
    ): Call<Contact>

    @POST("/contacts")
    fun createContact(@Body contact: Contact): Call<Contact>

    @PUT("/contacts/{id}")
    fun updateContact(@Path("id") id: Long, @Body contact: Contact): Call<Contact>

    @DELETE("/contacts/{id}")
    fun deleteContact(@Path("id") id: Long): Call<Void>
}
