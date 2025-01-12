package ru.bekmnsrw.compose.performance.analyzer.search.image

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * @author bekmnsrw
 */
interface ImageSearcherApi {

    @GET
    suspend fun fetchImage(@Url url: String): Response<ResponseBody>
}