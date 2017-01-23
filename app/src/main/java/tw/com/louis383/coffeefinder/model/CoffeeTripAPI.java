package tw.com.louis383.coffeefinder.model;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tw.com.louis383.coffeefinder.BuildConfig;
import tw.com.louis383.coffeefinder.Utils;
import tw.com.louis383.coffeefinder.model.domain.CoffeeShop;

/**
 * Created by louis383 on 2017/1/16.
 */

public class CoffeeTripAPI {

    private String HOST = BuildConfig.HOST;
    private OkHttpClient httpClient;

    private CoffeeTripService service;

    public CoffeeTripAPI() {
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        if (Utils.isDebuggingBuild()) {
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        }

        httpClient = new OkHttpClient.Builder()
                .addInterceptor(logInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        service = retrofit.create(CoffeeTripService.class);
    }

    public Observable<Response<List<CoffeeShop>>> getCoffeeShops(double latitude, double longitude, int range) {
        return service.getCoffeeShops(latitude, longitude, range)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
