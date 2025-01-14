/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : This object is responsible for setting up and providing a Retrofit instance for
 *               interacting with the server's REST API. It includes:
 *               - A base URL for the API.
 *               - A Gson converter for serializing and deserializing JSON.
 *               - A lazy-loaded instance of the ContactsApiService interface, which defines the API endpoints.
 */

import com.google.gson.GsonBuilder
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "https://daa.iict.ch"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val service: ContactsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ContactsApiService::class.java)
    }
}
