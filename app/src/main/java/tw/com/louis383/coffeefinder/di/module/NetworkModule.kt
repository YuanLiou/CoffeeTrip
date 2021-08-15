package tw.com.louis383.coffeefinder.di.module

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import tw.com.louis383.coffeefinder.BuildConfig
import tw.com.louis383.coffeefinder.model.ConnectivityChecker
import tw.com.louis383.coffeefinder.model.data.CoffeeTripService
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier


@Retention(AnnotationRetention.BINARY)
@Qualifier
private annotation class InternalNetworkApi

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    fun provideConnectivityChecker(
        @ApplicationContext context: Context
    ): ConnectivityChecker {
        return ConnectivityChecker(context)
    }

    @Provides
    @InternalNetworkApi
    fun provideLogInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().also {
            if (BuildConfig.DEBUG) {
                it.level = HttpLoggingInterceptor.Level.HEADERS
                it.level = HttpLoggingInterceptor.Level.BODY
            } else {
                it.level = HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @InternalNetworkApi
    fun provideHttpClient(
        @InternalNetworkApi logInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @InternalNetworkApi
    fun provideRetrofit(
        @InternalNetworkApi httpClient: Lazy<OkHttpClient>
    ): Retrofit {
        val HOST = BuildConfig.HOST
        val contentType = MediaType.parse("application/json")!!
        return Retrofit.Builder()
            .baseUrl(HOST)
            .addConverterFactory(Json.asConverterFactory(contentType))
            .callFactory {
                httpClient.get().newCall(it)
            }
            .build()
    }

    @Provides
    fun provideCoffeeTripApi(
        @InternalNetworkApi retrofit: Retrofit
    ): CoffeeTripService {
        return retrofit.create(CoffeeTripService::class.java)
    }
}