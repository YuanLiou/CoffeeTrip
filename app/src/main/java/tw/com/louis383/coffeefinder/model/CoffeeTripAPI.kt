package tw.com.louis383.coffeefinder.model

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tw.com.louis383.coffeefinder.BuildConfig
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop
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

        val retrofit = Retrofit.Builder()
                .baseUrl(HOST)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()

        service = retrofit.create(CoffeeTripService::class.java)
    }

    fun getCoffeeShops(latitude: Double, longitude: Double, range: Int): Observable<Response<List<CoffeeShop>>> {
        return service.getCoffeeShops(latitude, longitude, range) .observeOn(AndroidSchedulers.mainThread())
    }

}
