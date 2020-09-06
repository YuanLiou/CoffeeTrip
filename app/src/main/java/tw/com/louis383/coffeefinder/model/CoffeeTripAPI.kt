package tw.com.louis383.coffeefinder.model

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import tw.com.louis383.coffeefinder.BuildConfig
import tw.com.louis383.coffeefinder.utils.Utils
import java.util.concurrent.TimeUnit

/**
 * Created by louis383 on 2017/1/16.
 */

class CoffeeTripAPI {

    private val HOST = BuildConfig.HOST
    private val httpClient: OkHttpClient

    private val service: CoffeeTripService

    init {
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
        if (Utils.isDebuggingBuild()) {
            logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }

        httpClient = OkHttpClient.Builder()
                .addInterceptor(logInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

        val contentType = MediaType.parse("application/json")!!
        val retrofit = Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(Json.asConverterFactory(contentType))
                .client(httpClient)
                .build()

        service = retrofit.create(CoffeeTripService::class.java)
    }

    suspend fun getCoffeeShops(latitude: Double, longitude: Double, range: Int) = service.getCoffeeShops(latitude, longitude, range)
}
