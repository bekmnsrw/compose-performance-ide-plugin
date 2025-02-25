package compose.performance.analyzer.kmp.image.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * @author i.bekmansurov
 */
interface ImageSearcherApi {

    @GET
    suspend fun fetchImage(@Url url: String): Response<ResponseBody>
}