package ru.bekmnsrw.compose.performance.analyzer.search.image

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author bekmnsrw
 */
object RetrofitClient {

    private val okHttpClient = OkHttpClient.Builder().build()
    private val gsonConverterFactory = GsonConverterFactory.create()

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("http://localhost/")
        .addConverterFactory(gsonConverterFactory)
        .build()

    val imageSearcherApi: ImageSearcherApi = retrofit.create(ImageSearcherApi::class.java)
}