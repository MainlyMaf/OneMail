package com.onemail.app.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface MailTmApi {

    @GET("domains")
    suspend fun getDomains(): JsonElement

    @POST("accounts")
    suspend fun createAccount(@Body body: CreateAccountRequest): JsonElement

    @POST("token")
    suspend fun getToken(@Body body: TokenRequest): JsonElement

    @GET("messages")
    suspend fun getMessages(
        @Header("Authorization") authorization: String
    ): JsonElement

    @GET("messages/{id}")
    suspend fun getMessage(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): JsonElement

    @DELETE("accounts/{id}")
    suspend fun deleteAccount(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): Response<Unit>
}

object MailClient {

    private const val BASE_URL = "https://api.mail.tm/"

    private val gson: Gson = GsonBuilder().setLenient().create()

    val api: MailTmApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "OneMail/1.1 (Android)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MailTmApi::class.java)
    }
}